package org.xhtmlrenderer.pdf.impl;

import java.awt.image.BufferedImage;

import org.xhtmlrenderer.extend.FSImage;

public class PdfBoxFSImage implements FSImage {
	private static final int DOTS_PER_PIXEL = 20;

	BufferedImage bimg;
	int width;
	int height;

	public PdfBoxFSImage(BufferedImage bimg) {
		this.bimg = bimg;
		this.width = bimg.getWidth();
		this.height = bimg.getHeight();
	}

	@Override
	public int getWidth() {
		return width * DOTS_PER_PIXEL;
	}

	@Override
	public int getHeight() {
		return height * DOTS_PER_PIXEL;
	}

	@Override
	public void scale(int newWidth, int newHeight) {
		if (width > 0 || height > 0) {
			int currentWith = width;
			int currentHeight = height;
			this.width = newWidth;
			this.height = newHeight;

			if (newWidth == -1) {
				this.width = (int) (currentWith * ((double) newHeight / currentHeight));
			}

			if (newHeight == -1) {
				this.height = (int) (currentHeight * ((double) newWidth / currentWith));
			}
		}
	}
	
	@Override
	public Object clone() {
        return new PdfBoxFSImage(bimg);
    }

}
