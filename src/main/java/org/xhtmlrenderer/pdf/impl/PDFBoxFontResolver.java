package org.xhtmlrenderer.pdf.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

/**
 * Font resolver.
 * 
 * This class implements the method resolveFont, which returns a FSFont, based
 * on a FontSpecification (family, weight, style, variant).
 * 
 * TODO : 
 * - public void addFont(String path, String encoding, boolean embedded)
 * - text-decoration: line-through;
 * 
 */
public class PDFBoxFontResolver implements FontResolver {
	private static Logger log = Logger.getLogger("PDFBoxFontResolver");
	private Map<String, FontFamily> fontFamilies = createInitialFontMap();
	private Map<String, FontDescription> fontCache = new HashMap<>();

	public PDFBoxFontResolver(SharedContext _sharedContext) {
		log.info("Creating new FontResolver");
	}

	@Override
	public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
		log.info("resolveFont " + spec);
		return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
	}

	private FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
		if (!(style == IdentValue.NORMAL || style == IdentValue.OBLIQUE || style == IdentValue.ITALIC)) {
			style = IdentValue.NORMAL;
		}
		if (families != null) {
			for (int i = 0; i < families.length; i++) {
				FSFont font = resolveFont(ctx, families[i], size, weight, style, variant);
				if (font != null) {
					return font;
				}
			}
		}

		return resolveFont(ctx, "Serif", size, weight, style, variant);
	}

	private FSFont resolveFont(SharedContext ctx, String fontFamily, float size, IdentValue weight, IdentValue style, IdentValue variant) {
		String normalizedFontFamily = normalizeFontFamily(fontFamily);

		String cacheKey = getHashName(normalizedFontFamily, weight, style);
		FontDescription result = fontCache.get(cacheKey);

		if (result != null) {
			return new PdfBoxFSFont(result, size);
		}

		FontFamily family = fontFamilies.get(normalizedFontFamily);
		if (family != null) {
			result = family.match(convertWeightToInt(weight), style);
			if (result != null) {
				fontCache.put(cacheKey, result);
				return new PdfBoxFSFont(result, size);
			}
		}

		return null;
	}

	private String normalizeFontFamily(String fontFamily) {
		String result = fontFamily;
		// strip off the "s if they are there
		if (result.startsWith("\"")) {
			result = result.substring(1);
		}
		if (result.endsWith("\"")) {
			result = result.substring(0, result.length() - 1);
		}

		// normalize the font name
		if (result.equalsIgnoreCase("serif")) {
			result = "Serif";
		} else if (result.equalsIgnoreCase("sans-serif")) {
			result = "SansSerif";
		} else if (result.equalsIgnoreCase("monospace")) {
			result = "Monospaced";
		}

		return result;
	}

	public static int convertWeightToInt(IdentValue weight) {
		if (weight == IdentValue.NORMAL) {
			return 400;
		} else if (weight == IdentValue.BOLD) {
			return 700;
		} else if (weight == IdentValue.LIGHTER) {
			return 400;
		} else if (weight == IdentValue.BOLDER) {
			return 700;
		} else if (weight == IdentValue.FONT_WEIGHT_100) {
			return 100;
		} else if (weight == IdentValue.FONT_WEIGHT_200) {
			return 200;
		} else if (weight == IdentValue.FONT_WEIGHT_300) {
			return 300;
		} else if (weight == IdentValue.FONT_WEIGHT_400) {
			return 400;
		} else if (weight == IdentValue.FONT_WEIGHT_500) {
			return 500;
		} else if (weight == IdentValue.FONT_WEIGHT_600) {
			return 600;
		} else if (weight == IdentValue.FONT_WEIGHT_700) {
			return 700;
		} else if (weight == IdentValue.FONT_WEIGHT_800) {
			return 800;
		} else if (weight == IdentValue.FONT_WEIGHT_900) {
			return 900;
		}
		throw new IllegalArgumentException();
	}

	protected static String getHashName(String name, IdentValue weight, IdentValue style) {
		return name + "-" + weight + "-" + style;
	}

	@Override
	public void flushCache() {
		log.info("flushCache");
	}

	// Filling base font family cache with the standard Type 1 fonts.
	private Map<String, FontFamily> createInitialFontMap() {
		Map<String, FontFamily> res = new HashMap<>();
		addCourier(res);
		addTimes(res);
		addHelvetica(res);
		addSymbol(res);
		addZapfDingbats(res);
		return res;
	}

	private void addCourier(Map<String, FontFamily> map) {
		FontFamily courier = new FontFamily();
		courier.addFontDescription(new FontDescription(PDType1Font.COURIER_BOLD_OBLIQUE, IdentValue.OBLIQUE, 700));
		courier.addFontDescription(new FontDescription(PDType1Font.COURIER_OBLIQUE, IdentValue.OBLIQUE, 400));
		courier.addFontDescription(new FontDescription(PDType1Font.COURIER_BOLD, IdentValue.NORMAL, 700));
		courier.addFontDescription(new FontDescription(PDType1Font.COURIER, IdentValue.NORMAL, 400));

		map.put("DialogInput", courier);
		map.put("Monospaced", courier);
		map.put("Courier", courier);
	}

	private static void addTimes(Map<String, FontFamily> map) {
		FontFamily times = new FontFamily();
		times.addFontDescription(new FontDescription(PDType1Font.TIMES_BOLD_ITALIC, IdentValue.ITALIC, 700));
		times.addFontDescription(new FontDescription(PDType1Font.TIMES_ITALIC, IdentValue.ITALIC, 400));
		times.addFontDescription(new FontDescription(PDType1Font.TIMES_BOLD, IdentValue.NORMAL, 700));
		times.addFontDescription(new FontDescription(PDType1Font.TIMES_ROMAN, IdentValue.NORMAL, 400));

		map.put("Serif", times);
		map.put("TimesRoman", times);
	}

	private static void addHelvetica(Map<String, FontFamily> map) {
		FontFamily helvetica = new FontFamily();
		helvetica.addFontDescription(new FontDescription(PDType1Font.HELVETICA_BOLD_OBLIQUE, IdentValue.OBLIQUE, 700));
		helvetica.addFontDescription(new FontDescription(PDType1Font.HELVETICA_OBLIQUE, IdentValue.OBLIQUE, 400));
		helvetica.addFontDescription(new FontDescription(PDType1Font.HELVETICA_BOLD, IdentValue.NORMAL, 700));
		helvetica.addFontDescription(new FontDescription(PDType1Font.HELVETICA, IdentValue.NORMAL, 400));

		map.put("Dialog", helvetica);
		map.put("SansSerif", helvetica);
		map.put("Helvetica", helvetica);
	}

	private static void addSymbol(Map<String, FontFamily> map) {
		FontFamily fontFamily = new FontFamily();
		fontFamily.addFontDescription(new FontDescription(PDType1Font.SYMBOL, IdentValue.NORMAL, 400));

		map.put("Symbol", fontFamily);
	}

	private static void addZapfDingbats(Map<String, FontFamily> map) {
		FontFamily fontFamily = new FontFamily();
		fontFamily.addFontDescription(new FontDescription(PDType1Font.ZAPF_DINGBATS, IdentValue.NORMAL, 400));

		map.put("ZapfDingbats", fontFamily);
	}

	private static class FontFamily {
		private List<FontDescription> fontDescriptions = new ArrayList<>();;

		public FontFamily() {
		}

		public void addFontDescription(FontDescription descr) {
			fontDescriptions.add(descr);
		}

		public FontDescription match(int desiredWeight, IdentValue style) {
			if (fontDescriptions.size() == 0) {
				throw new RuntimeException("fontDescriptions is empty");
			}

			List<FontDescription> candidates = new ArrayList<>();

			for (FontDescription fd : fontDescriptions) {
				if (fd.getStyle() == style) {
					candidates.add(fd);
				}
			}

			if (candidates.size() == 0) {
				if (style == IdentValue.ITALIC) {
					return match(desiredWeight, IdentValue.OBLIQUE);
				} else if (style == IdentValue.OBLIQUE) {
					return match(desiredWeight, IdentValue.NORMAL);
				} else {
					candidates.addAll(fontDescriptions);
				}
			}

			// Find exact weight
			FontDescription result = findExactWeight(candidates, desiredWeight);
			if (result != null)
				return result;

			// Find closest weight
			result = findClosestWeight(candidates, desiredWeight);
			return result;
		}

		private FontDescription findClosestWeight(List<FontDescription> candidates, int desiredWeight) {
			int delta = Math.abs(candidates.get(0).weight - desiredWeight);
			FontDescription res = candidates.get(0);
			for (int i = 1; i < candidates.size(); i++) {
				FontDescription fd = candidates.get(i);
				int d = Math.abs(fd.getWeight() - desiredWeight);
				if (d < delta) {
					delta = d;
					res = fd;
				}
			}
			return res;
		}

		private FontDescription findExactWeight(List<FontDescription> candidates, int desiredWeight) {
			for (FontDescription fd : candidates) {
				if (fd.getWeight() == desiredWeight) {
					return fd;
				}
			}
			return null;
		}

	}

	public static class FontDescription {
		PDFont font;
		IdentValue style;
		int weight;
		int yStrikeoutPosition;
		int yStrikeoutSize;
		int underlinePosition;
		int underlineThickness;

		public FontDescription(PDFont font) {
			this(font, IdentValue.NORMAL, 400);
		}

		public FontDescription(PDFont font, IdentValue style, int weight) {
			this.font = font;
			this.style = style;
			this.weight = weight;
			underlinePosition = -50;
			underlineThickness = 50;

			try {
				float h = font.getHeight('x');
					yStrikeoutPosition = (int) (h / 2 + 50);
					yStrikeoutSize = 100;
			} catch (IOException e) {
				log.severe("IOException "+e.getMessage());
				yStrikeoutPosition = 0;
				yStrikeoutSize = 0;
			}
		}

		public PDFont getFont() {
			return font;
		}

		public int getWeight() {
			return weight;
		}

		public IdentValue getStyle() {
			return style;
		}

		public int getYStrikeoutPosition() {
			return yStrikeoutPosition;
		}

		public int getYStrikeoutSize() {
			return underlinePosition;
		}

		public int getUnderlinePosition() {
			return underlinePosition;
		}

		public float getUnderlineThickness() {
			return underlineThickness;
		}
	}
}
