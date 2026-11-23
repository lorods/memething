package br.edu.uniritter.memething;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.io.File;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main {

	protected static class EncapsuladorEntradaEdicao {
		private BufferedImage[] conjimg;
		private String[] conjlegend;
		private String ladoedicaovert, direscrita, nmarqescrit;

		protected EncapsuladorEntradaEdicao() {

		}

		protected EncapsuladorEntradaEdicao(BufferedImage[] conjimg, String[] conjlegend) {
			this.conjimg = conjimg;
			this.conjlegend = conjlegend;
		}

		protected EncapsuladorEntradaEdicao(BufferedImage[] conjimg, String[] conjlegend, String ladoeditvert) {
			this.conjimg = conjimg;
			this.conjlegend = conjlegend;
			this.ladoedicaovert = ladoeditvert;
			this.direscrita = direscrita;
			this.nmarqescrit = nmarqescrit;
		}

		protected BufferedImage[] getConjimg() {
			return conjimg;
		}

		protected void setConjimg(BufferedImage[] conjimg) {
			this.conjimg = conjimg;
		}

		protected String[] getConjlegend() {
			return conjlegend;
		}

		protected void setConjlegend(String[] conjlegend) {
			this.conjlegend = conjlegend;
		}

		protected String getLadoedicaovert() {
			return ladoedicaovert;
		}

		protected void setLadoedicaovert(String ladoeditvert) {
			this.ladoedicaovert = ladoeditvert;
		}

		protected String getDirescrita() {
			return direscrita;
		}

		protected void setDirescrita(String direscrita) {
			this.direscrita = direscrita;
		}

		protected String getNmarqescrit() {
			return nmarqescrit;
		}

		protected void setNmarqescrit(String nmarqescrit) {
			this.nmarqescrit = nmarqescrit;
		}
	}

	enum LadoEditavelImg {
		TOPO {
			@Override
			public String toString() {
				return "top";
			}
		},
		BAIXO {
			@Override
			public String toString() {
				return "bottom";
			}
		}
	}

	/*
	 * Regras de negócio: 1: Processar ao menos uma string do arquivo com frases de
	 * legenda e no máximo 100 MB do arquivo, caso contrário pular restantes 2:
	 * Processar ao menos uma imagem do caminho fornecido como argumento e no máximo
	 * uma pasta com 500 MB em imagens, caso contrário pular restantes 3: Receber no
	 * mínimo quatro argumentos conhecidos e no máximo seis argumentos conhecidos
	 */

	private static final String CAPTIONLSFLAG = "--captionls", IMGDIRFLAG = "--imgdir", EDITEDEDGEFLAG = "--edge";

	public static void main(String[] args) throws IOException {
		final int COMPRIM_MIN_VETSTR = 1;
		final int TAM_MAX_MB_ARQSTR = 100;
		final int COMPRIM_MIN_VETIMG = 1;
		final int TAM_MAX_MB_DIRIMG = 500;
		final int COMPRIM_MIN_ARG = 4;
		final int COMPRIM_MAX_ARG = 6;
		final int COMPRIM_REAL = args.length;
		String razaoerro = aferirIntervaloComprimento(COMPRIM_MIN_ARG, COMPRIM_MAX_ARG, COMPRIM_REAL);
		if (razaoerro != null) {
			System.out.printf("Erro: %s\n", razaoerro);
			return;
		}
		EncapsuladorEntradaEdicao conjartefato = manipularComposicaoArg(args, COMPRIM_REAL);
		/*
		 * System.out.
		 * printf("Acesso intermediado:\n Total de imagens: %d,\nLegendas: %s,\nLado da edição: %s\n"
		 * , conjartefato.getConjimg().length,
		 * Arrays.toString(conjartefato.getConjlegend()),
		 * conjartefato.getLadoedicaovert());
		 */
		if (conjartefato != null) {
			BufferedImage imgaleat = obterImgAleatoria(conjartefato.getConjimg());
			Graphics ctxgrafimg = desenharStringEmImg(imgaleat, obterStringAleatoria(conjartefato.getConjlegend()),
					conjartefato.getLadoedicaovert());
			String seloatualdatahr = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss").withZone(ZoneId.systemDefault())
					.format(Instant.now());
			String direscrita = conjartefato.getDirescrita();
			File arqpescrita = new File(direscrita, "../../editado/meme_".concat(seloatualdatahr).concat(".png"));
			manipularArqSaida(arqpescrita);
			gravarImgEmArq(imgaleat, arqpescrita);
		}
	}

	private static String aferirIntervaloComprimento(int comprim_min, int comprim_max, int comprim_real) {
		String razaoretornocomp = comprim_real < comprim_min
				? "lista de argumentos insuficiente (mínimo são ".concat(String.valueOf(comprim_min)).concat(").")
				: (comprim_real > comprim_max
						? "lista de argumentos excessiva (máximo são ".concat(String.valueOf(comprim_max)).concat(").")
						: (comprim_real == 5 ? "lista de argumentos está incompleta; argumentos esperados: 4 ou 6."
								: null));
		return razaoretornocomp;
	}

	private static EncapsuladorEntradaEdicao manipularComposicaoArg(String[] lsarg, int comprim_real)
			throws IOException {
		boolean argtemarqfrase = false, argtemdirimg = false, ladoedicaoestadef = false, conjargestasuficiente = false;
		BufferedImage[] conjimg = new BufferedImage[0];
		String[] conjlegend = new String[0];
		String ladoedicaovert = new String();
		EncapsuladorEntradaEdicao conjartefretornado = new EncapsuladorEntradaEdicao();
		for (int i = 1; i <= comprim_real; i += 2) {
			if (!argtemdirimg) {
				if (lsarg[(i - 1)].equals(IMGDIRFLAG) && new File(lsarg[i]).isDirectory()) {
					conjimg = validarImgEmDir(lsarg[i]);
					for (int j = 0; j < conjimg.length; j++) {
						System.out.printf("\nOpacidade da imagem: %d\n", conjimg[j].getTransparency());
					}
					argtemdirimg = true;
					conjartefretornado.setDirescrita(lsarg[i]);
				}
			}

			if (!argtemarqfrase) {
				if (lsarg[(i - 1)].equals(CAPTIONLSFLAG) && new File(lsarg[i]).isFile()) {
					conjlegend = validarArqFrase(lsarg[i]);
					System.out.printf("Strings de frases: %s.", Arrays.toString(conjlegend));
					argtemarqfrase = true;
				}
			}

			if (!ladoedicaoestadef) {
				if (lsarg[(i - 1)].equals(EDITEDEDGEFLAG)) {
					ladoedicaovert = lsarg[i].equals(LadoEditavelImg.BAIXO.toString())
							? LadoEditavelImg.BAIXO.toString()
							: LadoEditavelImg.TOPO.toString();
					ladoedicaoestadef = true;
				}
			}
			if (argtemdirimg && argtemarqfrase) {
				conjartefretornado.setConjimg(conjimg);
				conjartefretornado.setConjlegend(conjlegend);

				if (ladoedicaoestadef) {
					conjartefretornado.setLadoedicaovert(ladoedicaovert);
					return conjartefretornado;
				}
				conjargestasuficiente = true;
			}
		}
		if (conjargestasuficiente)
			conjartefretornado.setLadoedicaovert(LadoEditavelImg.TOPO.toString());
		else
			conjartefretornado = null;
		return conjartefretornado;
	}

	private static BufferedImage[] validarImgEmDir(String caminhodir) throws IOException {
		File dirimg = new File(caminhodir);
		File[] lsarq = dirimg.listFiles((dir, name) -> !name.equals("out"));
		BufferedImage[] conjimg = new BufferedImage[0];
		for (int i = 0; i < lsarq.length; i++) {
			if (ImageIO.read(lsarq[i]) != null) {
				if (conjimg.length == 0)
					conjimg = new BufferedImage[] { ImageIO.read(lsarq[i]) };
				else {
					conjimg = Arrays.copyOf(conjimg, (conjimg.length + 1));
					conjimg[(conjimg.length - 1)] = ImageIO.read(lsarq[i]);
				}
			}
		}
		return conjimg;
	}

	private static String[] validarArqFrase(String caminhoarq) throws IOException {
		final String DELIMIT_FRASE = ";";
		File arqlegend = new File(caminhoarq);
		FileReader leitorarq = new FileReader(arqlegend);
		BufferedReader leitorsegmnt = new BufferedReader(leitorarq);
		String linhautil;
		String[] conjlegend = null;
		while ((linhautil = leitorsegmnt.readLine()) != null) {
			conjlegend = linhautil.split(DELIMIT_FRASE);
		}
		leitorarq.close();
		return conjlegend;
	}

	private static BufferedImage obterImgAleatoria(BufferedImage[] conjamostraimg) {
		return conjamostraimg[obterIntAleatSegur(conjamostraimg.length)];
	}

	private static String obterStringAleatoria(String[] conjlegend) {
		return conjlegend[obterIntAleatSegur(conjlegend.length)];
	}

	private static int obterIntAleatSegur(int comprim) {
		return new SecureRandom().nextInt(comprim);
	}

	private static Graphics desenharStringEmImg(BufferedImage imgaleat, String frase, String ladovertescrita) {
		Font fntsis = new Font("Noto Sans", Font.BOLD, 100);
		Graphics grafimg = imgaleat.getGraphics();
		FontMetrics inspetorfnt = grafimg.getFontMetrics(fntsis);
		GlyphVector vetglifo = fntsis.createGlyphVector(inspetorfnt.getFontRenderContext(), frase);
		float tamfntredimens = (float) manipularTamFnt(inspetorfnt, vetglifo, vetglifo.getOutline(0, 0), imgaleat,
				fntsis);
		if (tamfntredimens != 0) {
			fntsis = fntsis.deriveFont(fntsis.getStyle(), tamfntredimens);
			inspetorfnt = grafimg.getFontMetrics(fntsis);
		}
		int posX = calcPosicHorizonLegend(imgaleat.getWidth(), inspetorfnt.stringWidth(frase)),
				posY = LadoEditavelImg.BAIXO.toString().equals(ladovertescrita)
						? imgaleat.getHeight() - inspetorfnt.getAscent()
						: inspetorfnt.getAscent() * 2;
		grafimg.setFont(fntsis);
		grafimg.setColor(Color.BLACK);
		grafimg.drawString(frase, posX, posY);
		return grafimg;
	}

	private static void manipularArqSaida(File arqsaida) {
		if (arqsaida.exists())
			return;
		else
			arqsaida.getParentFile().mkdirs();
	}

	private static void gravarImgEmArq(BufferedImage imgmeme, File arqagravar) throws IOException {
		ImageIO.write(imgmeme, "png", arqagravar);
		System.out.printf("\nUm novo meme foi gravado com sucesso.\n");
	}

	private static double manipularTamFnt(FontMetrics inspetorfnt, GlyphVector vetglifo, Shape contorno,
			BufferedImage imgamostra, Font fntsis) {
		double largtxtnormal = contorno.getBounds().getWidth(), alturatxtnormal = contorno.getBounds().getHeight();
		int largimg = imgamostra.getWidth();
		int alturaimg = imgamostra.getHeight();
		boolean margemconfortavel = largimg > largtxtnormal * 1.05 && alturaimg > alturatxtnormal * 1.05;
		if (margemconfortavel)
			return 0;
		else {
			double tamfntreflarg = (fntsis.getSize2D() * largimg) / largtxtnormal,
					tamfntrefaltura = (fntsis.getSize2D() * alturaimg) / alturatxtnormal;
			return (tamfntreflarg < tamfntrefaltura) ? tamfntreflarg : tamfntrefaltura;
		}
	}

	private static int calcPosicHorizonLegend(int largimg, int largtxt) {
		return new BigDecimal(largimg).subtract(new BigDecimal(largtxt)).divide(BigDecimal.TWO, RoundingMode.HALF_UP)
				.intValue();
	}
}