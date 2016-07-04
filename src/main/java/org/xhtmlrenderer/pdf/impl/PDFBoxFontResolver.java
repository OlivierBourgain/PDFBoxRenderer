package org.xhtmlrenderer.pdf.impl;

import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

public class PDFBoxFontResolver implements FontResolver {
	private static Logger log = Logger.getLogger("PDFBoxFontResolver");

	public PDFBoxFontResolver(SharedContext _sharedContext) {
		log.info("Creating new FontResolver");
	}

	@Override
	public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
		log.info("resolveFont " + spec);
		return new PdfBoxFSFont(PDType1Font.TIMES_ROMAN, spec.size);
	}

	@Override
	public void flushCache() {
		log.info("flushCache");
	}

}
