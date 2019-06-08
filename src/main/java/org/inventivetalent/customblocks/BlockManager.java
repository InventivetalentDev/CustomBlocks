/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.customblocks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.inventivetalent.customblocks.data.CoordinateImageData;
import org.inventivetalent.customblocks.data.ImageData;
import org.inventivetalent.imgur.ImgurUploader;
import org.inventivetalent.imgur.UploadCallback;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;
import org.mineskin.data.SkinCallback;
import org.mineskin.data.SkinData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlockManager {

	static final long UPLOAD_TIMEOUT = 30000;
	static final long SKULL_TIMEOUT  = 30000 * 9;

	private CustomBlocks plugin;

	private MineskinClient skinClient ;

	private Executor createExecutor = Executors.newSingleThreadExecutor();

	public BlockManager(CustomBlocks plugin) {
		this.plugin = plugin;
		skinClient = new MineskinClient();
	}

	public void createBlock(final String name, final String image, final ImageDownloadCallback imageDownloadCallback, final UploadCallback uploadCallback, final SkinCallback skinCallback, final BlockCallback blockCallback) throws MalformedURLException {
		checkNotNull(name);
		checkNotNull(image);
		checkNotNull(uploadCallback);
		checkNotNull(skinCallback);

		final URL imageUrl = new URL(image);

		createExecutor.execute(new Runnable() {
			@Override
			public void run() {
				// Download & convert original image
				final BufferedImage original;
				BufferedImage[][][] converted;
				try {
					original = ImageIO.read(imageUrl);
					converted = ImageConverter.convertImage(original);
				} catch (IOException e) {
					imageDownloadCallback.exception(e);
					throw new RuntimeException(e);
				}
				imageDownloadCallback.done();

				// Upload converted images to imgur
				final String[] originalUrl = new String[1];
				final String[][][] urls = new String[2][2][2];
				try {
					final CountDownLatch uploadLatch = new CountDownLatch(9/* converted + original */);
					final boolean[] uploadFailed = new boolean[1];
					for (int x = 0; x < 2; x++) {
						for (int y = 0; y < 2; y++) {
							for (int z = 0; z < 2; z++) {
								final int finalX = x;
								final int finalY = y;
								final int finalZ = z;
								if (plugin.debug) {
									plugin.getLogger().info("Uploading Image " + x + "," + y + "," + z);
								}
								ImgurUploader.upload(plugin.imgurClientId, converted[x][y][z], new UploadCallback() {
									@Override
									public void exception(Throwable throwable) {
										uploadFailed[0] = true;
										uploadCallback.exception(throwable);
										uploadLatch.countDown();
									}

									@Override
									public void uploaded(Map<String, String> map, JsonElement jsonElement) {
										urls[finalX][finalY][finalZ] = jsonElement.getAsJsonObject().getAsJsonObject("data").get("link").getAsString();
										uploadLatch.countDown();

										if (plugin.debug) {
											plugin.getLogger().info("Uploaded Image " + finalX + "," + finalY + "," + finalZ + ": " + urls[finalX][finalY][finalZ]);
										}
									}
								});
							}
						}
					}
					if (plugin.debug) {
						plugin.getLogger().info("Uploading base Image");
					}
					ImgurUploader.upload(plugin.imgurClientId, original, new UploadCallback() {
						@Override
						public void exception(Throwable throwable) {
							uploadFailed[0] = true;
							uploadCallback.exception(throwable);
							uploadLatch.countDown();
						}

						@Override
						public void uploaded(Map<String, String> map, JsonElement jsonElement) {
							originalUrl[0] = jsonElement.getAsJsonObject().getAsJsonObject("data").get("link").getAsString();
							uploadLatch.countDown();
							if (plugin.debug) {
								plugin.getLogger().info("Uploaded base Image: " + originalUrl[0]);
							}
						}
					});
					uploadLatch.await(UPLOAD_TIMEOUT, TimeUnit.MILLISECONDS);
					if (uploadFailed[0]) {
						uploadCallback.exception(new RuntimeException("upload failed"));
						return;
					}
				} catch (Exception e) {
					uploadCallback.exception(e);
					throw new RuntimeException(e);
				}
				uploadCallback.uploaded(null, null);

				//Convert the images to skin data
				final SkinData[] originalData = new SkinData[1];
				final SkinData[][][] data = new SkinData[2][2][2];
				try {
					final CountDownLatch skullLatch = new CountDownLatch(9/* converted + original */);
					final boolean[] skullFailed = new boolean[1];
					for (int x = 0; x < 2; x++) {
						for (int y = 0; y < 2; y++) {
							for (int z = 0; z < 2; z++) {
								final int finalX = x;
								final int finalY = y;
								final int finalZ = z;
								skinClient.generateUrl(urls[x][y][z], new SkinCallback() {
									@Override
									public void done(Skin skin) {
										data[finalX][finalY][finalZ] = skin.data;
										skullLatch.countDown();

										if (plugin.debug) {
											plugin.getLogger().info("Generated skull " + finalX + "," + finalY + "," + finalZ);
										}
									}

									@Override
									public void waiting(long l) {
										if (plugin.debug) {
											plugin.getLogger().info("[waiting] " + finalX + "," + finalY + "," + finalZ + ": " + l);
										}
										skinCallback.waiting(l);
									}

									@Override
									public void uploading() {
										if (plugin.debug) {
											plugin.getLogger().info("[uploading] " + finalX + "," + finalY + "," + finalZ);
										}
										skinCallback.uploading();
									}

									@Override
									public void error(String s) {
										skullFailed[0] = true;
										skinCallback.error(s);
										skullLatch.countDown();
									}

								});
							}
						}
					}
					skinClient.generateUrl(originalUrl[0], new SkinCallback() {
						@Override
						public void waiting(long l) {
							skinCallback.waiting(l);
						}

						@Override
						public void uploading() {
							skinCallback.uploading();
						}

						@Override
						public void error(String s) {
							skullFailed[0] = true;
							skinCallback.error(s);
							skullLatch.countDown();
						}

						@Override
						public void done(Skin skin) {
							originalData[0] = skin.data;
							skullLatch.countDown();
						}
					});
					skullLatch.await(SKULL_TIMEOUT, TimeUnit.MILLISECONDS);
					if (skullFailed[0]) {
						skinCallback.error("failed");
						return;
					}
				} catch (Exception e) {
					skinCallback.error(e.getMessage());
					throw new RuntimeException(e);
				}
				skinCallback.done(null);

				ImageData baseData = ImageData.fromProperty(originalUrl[0], originalData[0].texture);
				Set<CoordinateImageData> imageDatas = new HashSet<>();
				for (int x = 0; x < 2; x++) {
					for (int y = 0; y < 2; y++) {
						for (int z = 0; z < 2; z++) {
							SkinData skullData = data[x][y][z];
							imageDatas.add(CoordinateImageData.from(ImageData.fromProperty(urls[x][y][z], skullData.texture), x, y, z));
						}
					}
				}
				CustomBlock customBlock = new CustomBlock(name, baseData, imageDatas);
				blockCallback.done(customBlock);
			}
		});
	}

	public File saveBlock(CustomBlock customBlock) {
		File file = getBlockFile(customBlock.getName());
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			try (Writer writer = new FileWriter(file)) {
				new Gson().toJson(customBlock, writer);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	public boolean doesBlockExist(String name) {
		File file = getBlockFile(name);
		return file != null && file.exists();
	}

	public CustomBlock loadBlock(String name) {
		File file = getBlockFile(name);
		try {
			if (!file.exists()) {
				return null;
			}
			try (Reader reader = new FileReader(file)) {
				return new Gson().fromJson(reader, CustomBlock.class);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	File getBlockFile(String name) {
		name = name.toLowerCase();
		return new File(plugin.savesFolder, name + ".cb");
	}

}
