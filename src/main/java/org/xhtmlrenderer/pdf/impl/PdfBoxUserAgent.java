/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

import javax.imageio.ImageIO;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

public class PdfBoxUserAgent extends NaiveUserAgent {
	private static final int IMAGE_CACHE_CAPACITY = 32;

	public PdfBoxUserAgent() {
		super(IMAGE_CACHE_CAPACITY);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ImageResource getImageResource(String uriStr) {
		if (ImageUtil.isEmbeddedBase64Image(uriStr)) {
			return loadEmbeddedBase64ImageResource(uriStr);
		}

		uriStr = resolveURI(uriStr);
		ImageResource resource = (ImageResource) _imageCache.get(uriStr);
		if (resource != null) {
			FSImage image = resource.getImage();
			if (image instanceof PdfBoxFSImage) {
				image = (FSImage) ((PdfBoxFSImage) resource.getImage()).clone();
			}
			return new ImageResource(resource.getImageUri(), image);
		}

		try (InputStream is = resolveAndOpenStream(uriStr)) {
			URI uri = new URI(uriStr);
			if (uri.getPath() != null && uri.getPath().toLowerCase().endsWith(".pdf")) {
				throw new UnsupportedOperationException("format not supported");
			} else {
				BufferedImage bimg = ImageIO.read(new File(uri));
				PdfBoxFSImage img = new PdfBoxFSImage(bimg);
				resource = new ImageResource(uriStr, img);
				_imageCache.put(uriStr, resource);
			}
			return resource;
		} catch (Exception e) {
			XRLog.exception("Can't read image file; unexpected problem for URI '" + uriStr + "'", e);
			return null;
		}
	}

	private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
		try {
			// byte[] buffer = ImageUtil.getEmbeddedBase64Image(uri);
			// Image image = Image.getInstance(buffer);
			// scaleToOutputResolution(image);
			// return new ImageResource(null, new ITextFSImage(image));
		} catch (Exception e) {
			XRLog.exception("Can't read XHTML embedded image.", e);
		}
		return new ImageResource(null, null);
	}

}
