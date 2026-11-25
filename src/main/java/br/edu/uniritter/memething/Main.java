package br.edu.uniritter.memething;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.awt.Point;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.io.File;
import java.security.SecureRandom;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main {

	enum OpcaoValidaMenu {
		CRIARMEME, DEFINIRFNTNAMEM, DEFINIRCORFNTNAMEM, ENCERRARPROG, INVALIDA
	}

	protected static class OperacaoComMeme {
		private BufferedImage[] conjimg;
		private String[] conjlegend;
		private String ladoedicaovert, direscrita, nmarqescrit;
		private boolean revisaoativ;
		private File[] lsarq;

		protected OperacaoComMeme() {

		}

		protected OperacaoComMeme(BufferedImage[] conjimg, String[] conjlegend) {
			this.conjimg = conjimg;
			this.conjlegend = conjlegend;
		}

		protected OperacaoComMeme(boolean revisaoativ) {
			this.revisaoativ = revisaoativ;
		}

		protected OperacaoComMeme(BufferedImage[] conjimg, String[] conjlegend, String ladoeditvert, String direscrita,
				String nmarqescrit, boolean revisaoativ) {
			this.conjimg = conjimg;
			this.conjlegend = conjlegend;
			this.ladoedicaovert = ladoeditvert;
			this.direscrita = direscrita;
			this.nmarqescrit = nmarqescrit;
			this.revisaoativ = revisaoativ;
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

		protected boolean temRevisaoativ() {
			return revisaoativ;
		}

		protected void setRevisaoativ(boolean revisaoativ) {
			this.revisaoativ = revisaoativ;
		}

		protected File[] getLsarq() {
			return lsarq;
		}

		protected void setLsarq(File[] lsarq) {
			this.lsarq = lsarq;
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
	 * legenda e no máximo 32 KB do arquivo, caso contrário pular restantes 2:
	 * Processar uma imagem com no mínimo x bytes e no máximo 
	 * com 5000 KB, caso contrário pulá-la 3: Receber no mínimo quatro
	 * argumentos conhecidos e no máximo seis argumentos conhecidos
	 */

	private static final String CAPTION_LS_FLAG = "--captionls", IMG_DIR_FLAG = "--imgdir", EDITED_EDGE_FLAG = "--edge",
			REVIEW_MODE_FLAG = "--reviewtime", HELP_FLAG = "--help";
	private static final int COMPRIM_MIN_VETSTR = 1, TAM_MAX_KB_ARQ_STR = Integer.SIZE, COMPRIM_MIN_VETIMG = 1,
			COMPRIM_MIN_ARG = 4, COMPRIM_MAX_ARG = 6;
	private static final BigDecimal TAM_MAX_KB_ARQ_IMG = new BigDecimal(5000);

	public static void main(String[] args)
			throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		final int COMPRIM_REAL = args.length;
		execOperacionalizacao(COMPRIM_MIN_ARG, COMPRIM_MAX_ARG, COMPRIM_REAL, args);
	}

	private static void execOperacionalizacao(int comprim_min_arg, int comprim_max_arg, int comprim_real, String[] args)
			throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (Arrays.asList(args).contains(HELP_FLAG)) {
			System.out.printf(
					"===Ajuda do programa===\nAbaixo, colchetes denotam um parâmetro facultativo:\n\n--imgdir <caminho>\tpasta que contém as imagens de amostra, uma sorteada por meme\n--captionls <caminho>\tarquivo com strings para a legenda separadas por ponto e vírgula\n[--edge <top|bottom>]\to lado vertical próximo do qual a legenda deve ser inserida\n[--reviewtime]\tperguntar se o valor sorteado deve ser descartado ou usado\n[--help]\texibir esta mensagem de ajuda e encerrar");
			return;
		}
		Scanner escaneadr = new Scanner(System.in);
		String razaoerro = aferirIntervaloComprimento(comprim_min_arg, comprim_max_arg, comprim_real);
		if (razaoerro != null) {
			System.out.printf("Erro: %s\n", razaoerro);
			return;
		}
		OperacaoComMeme conjartefato = manipularComposicaoArg(args, comprim_real);
		/*
		 * System.out.
		 * printf("Acesso intermediado:\n Total de imagens: %d,\nLegendas: %s,\nLado da edição: %s\n"
		 * , conjartefato.getConjimg().length,
		 * Arrays.toString(conjartefato.getConjlegend()),
		 * conjartefato.getLadoedicaovert());
		 */
		int selecaoescolhamenu;
		AttributedString txtcomatr = new AttributedString(new String("-"));
		do {
			selecaoescolhamenu = encaminharOpcaoMenu(escaneadr, conjartefato, txtcomatr);
		} while (selecaoescolhamenu != -1);
		escaneadr.close();
	}

	private static int encaminharOpcaoMenu(Scanner escaneadr, OperacaoComMeme conjartefato, AttributedString txtcomatr)
			throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		System.out.printf(
				"As operações disponíveis neste programa são as seguintes:\n\n1 — Criar meme com as configurações atuais\n2 — Definir fonte tipográfica para os próximos memes (até o programa sair)\n3 — Definir cor da fonte tipográfica para os próximos memes (até o programa sair)\n4 — Encerrar programa\n\nDigite o código (1–4) correspondente à opção desejada: ");
		int selecaomenu = escaneadr.nextInt();
		selecaomenu--;
		OpcaoValidaMenu[] conjopc = OpcaoValidaMenu.values();
		OpcaoValidaMenu opcao = (selecaomenu >= 0 && selecaomenu < (conjopc.length - 1)) ? conjopc[selecaomenu]
				: OpcaoValidaMenu.INVALIDA;
		switch (opcao) {
		case CRIARMEME -> {
			if (conjartefato != null) {
				Map<Attribute, Object> conjatr = txtcomatr.getIterator().getAttributes();
				BufferedImage imgaleat = obterImgAleatoria(conjartefato.getConjimg(), conjartefato.getLsarq(),
						conjartefato.temRevisaoativ(), escaneadr);
				String frasesel = obterFraseAleatoria(conjartefato.getConjlegend(), conjartefato.temRevisaoativ(),
						escaneadr);
				txtcomatr = new AttributedString(frasesel);
				txtcomatr.addAttributes(conjatr, 0, frasesel.length());
				Graphics ctxgrafimg = desenharStringEmImg(imgaleat, frasesel, conjartefato.getLadoedicaovert(),
						!txtcomatr.getIterator().getAllAttributeKeys().isEmpty() ? txtcomatr : null);
				String seloatualdatahr = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss").withZone(ZoneId.systemDefault())
						.format(Instant.now());
				String direscrita = conjartefato.getDirescrita();
				File arqpescrita = new File(direscrita, "../../editado/meme_".concat(seloatualdatahr).concat(".png"));
				manipularArqSaida(arqpescrita);
				gravarImgEmArq(imgaleat, arqpescrita);
				ctxgrafimg.dispose();
			}
		}
		case DEFINIRFNTNAMEM -> {
			System.out
					.printf("Insira ao lado o nome da fonte tipográfica para a legenda do meme (por exemplo, Arial): ");
			escaneadr.nextLine();
			String nomefamiliafnt = escaneadr.nextLine();
			boolean fntdisponivel = aferirDisponibilidadeFnt(nomefamiliafnt);
			if (!fntdisponivel) {
				System.out.printf("A fonte que você informou não pôde ser carregada de seu sistema.\n");
			} else
				txtcomatr.addAttribute(TextAttribute.FAMILY, nomefamiliafnt);
		}
		case DEFINIRCORFNTNAMEM -> {
			System.out.printf(
					"Insira ao lado a cor EM INGLÊS da fonte tipográfica para a legenda do meme (por exemplo, red): ");
			escaneadr.nextLine();
			String nomecorfnt = escaneadr.nextLine();
			Color cordisponivel = procurarFntLocal(nomecorfnt);
			if (cordisponivel == null)
				System.out.printf("A cor que você informou não pôde ser localizada.");
			else
				txtcomatr.addAttribute(TextAttribute.FOREGROUND, cordisponivel);
		}
		case ENCERRARPROG -> {
			return -1;
		}
		default -> {
			System.out.printf("Opção inválida! Listando operações novamente...\n");
			return -2;
		}
		}
		return selecaomenu;
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

	private static OperacaoComMeme manipularComposicaoArg(String[] lsarg, int comprim_real) throws IOException {
		boolean argtemarqfrase = false, argtemdirimg = false, ladoedicaoestadef = false, conjargestasuficiente = false;
		BufferedImage[] conjimg = new BufferedImage[0];
		String[] conjlegend = new String[0];
		String ladoedicaovert = new String();
		OperacaoComMeme conjartefretornado = Arrays.asList(lsarg).contains(REVIEW_MODE_FLAG) ? new OperacaoComMeme(true)
				: new OperacaoComMeme(false);
		File[] lsarq = new File[0];
		int[] indrm = new int[0];
		BigDecimal[][] lsprop = new BigDecimal[conjimg.length][2];
		for (int i = 1; i <= comprim_real; i += 2) {
			if (!argtemdirimg) {
				if (lsarg[(i - 1)].equals(IMG_DIR_FLAG) && new File(lsarg[i]).isDirectory()) {
					File dirimg = new File(lsarg[i]);
					lsarq = dirimg.listFiles((dir, name) -> !name.equals("out"));
					conjimg = validarImgEmDir(dirimg, lsarq);
					System.out.printf("O PROGRAMA FOI CARREGADO COM OS SEGUINTES ARQUIVOS E SUAS PROPRIEDADES:\n");
					for (int j = 0; j < conjimg.length; j++) {
						lsprop[j][0] = new BigDecimal(lsarq[j].length()).divide(new BigDecimal(1024 * 1024));
						if (lsprop[j][0].compareTo(TAM_MAX_KB_ARQ_IMG) == 1) {
							System.out.printf(
									"O arquivo %s excede o tamanho máximo de imagem individual processado por este programa (%s). Ele não será sorteado como base de meme.\n",
									lsarq[j].getAbsolutePath(), TAM_MAX_KB_ARQ_IMG.toPlainString());
							indrm = indrm.length == 0 ? new int[1] : Arrays.copyOf(indrm, (indrm.length + 1));
						}
					}
					int k = 0;
					List<File> lsauxarq = Arrays.asList(lsarq);
					List<BufferedImage> lsauximg = Arrays.asList(conjimg);
					for (int j = 0; j < indrm.length; j++) {
						lsauxarq.remove(indrm[j] - k);
						lsauximg.remove(indrm[j] - k);
						k++;
					}
					lsarq = File[].class.cast(lsauxarq.toArray());
					conjimg = BufferedImage[].class.cast(lsauximg.toArray());
					argtemdirimg = true;
					conjartefretornado.setDirescrita(lsarg[i]);
				}
			}

			if (!argtemarqfrase) {
				if (lsarg[(i - 1)].equals(CAPTION_LS_FLAG) && new File(lsarg[i]).isFile()) {
					conjlegend = validarArqFrase(lsarg[i]);
					System.out.printf("Strings de frases: %s.\n\n", Arrays.toString(conjlegend));
					argtemarqfrase = true;
				}
			}

			if (!ladoedicaoestadef) {
				if (lsarg[(i - 1)].equals(EDITED_EDGE_FLAG)) {
					ladoedicaovert = lsarg[i].equals(LadoEditavelImg.BAIXO.toString())
							? LadoEditavelImg.BAIXO.toString()
							: LadoEditavelImg.TOPO.toString();
					ladoedicaoestadef = true;
				}
			}
			if (argtemdirimg && argtemarqfrase) {
				conjartefretornado.setLsarq(lsarq);
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

	private static BufferedImage[] validarImgEmDir(File dirimg, File[] lsarq) throws IOException {
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

	private static BufferedImage obterImgAleatoria(BufferedImage[] conjamostraimg, File[] lsarq, boolean revisaoativ,
			Scanner escaneadr) {
		int aleatsegur;
		for (;;) {
			aleatsegur = obterIntAleatSegur(conjamostraimg.length);
			if (revisaoativ) {
				System.out.printf("A imagem aleatória selecionada está no caminho "
						.concat(lsarq[aleatsegur].getAbsolutePath())
						.concat(". Você deseja continuar com ela? Digite \"não\" se não quiser, caso contrário \"sim\" ou qualquer outro valor: "));
				String decisaorev = escaneadr.nextLine();
				if (!decisaorev.toLowerCase().equals("não"))
					break;
			} else
				break;
		}
		BufferedImage imgorig = conjamostraimg[aleatsegur];
		BufferedImage imgcopia = new BufferedImage(imgorig.getWidth(), imgorig.getHeight(),
				imgorig.getType() != 0 ? imgorig.getType() : BufferedImage.TYPE_INT_ARGB);
		Graphics g = imgcopia.createGraphics();
		g.drawImage(imgorig, 0, 0, null);
		g.dispose();
		return imgcopia;
	}

	private static String obterFraseAleatoria(String[] conjlegend, boolean revisaoativ, Scanner escaneadr) {
		int aleatsegur;
		for (;;) {
			aleatsegur = obterIntAleatSegur(conjlegend.length);
			if (revisaoativ) {
				System.out.printf("A frase aleatória selecionada é ".concat(conjlegend[aleatsegur]).concat(
						". Você deseja continuar com ela? Digite \"não\" se não quiser, caso contrário \"sim\" ou qualquer outro valor: "));
				String decisaorev = escaneadr.nextLine();
				if (!decisaorev.toLowerCase().equals("não"))
					break;
			} else
				break;
		}
		return conjlegend[aleatsegur];
	}

	private static int obterIntAleatSegur(int comprim) {
		return new SecureRandom().nextInt(comprim);
	}

	private static boolean aferirDisponibilidadeFnt(String nomefamiliafnt) {
		String[] bibfnt = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		return Arrays.asList(bibfnt).contains(nomefamiliafnt);
	}

	private static Color procurarFntLocal(String nomecorfnt)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		return Color.class.cast(Color.class.getField(nomecorfnt.trim().toUpperCase()).get(null));
	}

	private static Graphics desenharStringEmImg(BufferedImage imgaleat, String frase, String ladovertescrita,
			AttributedString atrtxt) throws NullPointerException {
		Object atrfamfnt = atrtxt != null ? atrtxt.getIterator().getAttribute(TextAttribute.FAMILY) : (Object) "Arial",
				atrcorfnt = atrtxt != null ? atrtxt.getIterator().getAttribute(TextAttribute.FOREGROUND) : Color.BLACK;
		Font fntsis = atrfamfnt != null ? new Font(atrfamfnt.toString(), Font.BOLD, 100)
				: new Font("Arial", Font.BOLD, 100);
		if (atrcorfnt == null)
			atrtxt.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
		Graphics grafimg = imgaleat.getGraphics();
		FontMetrics inspetorfnt = grafimg.getFontMetrics(fntsis);
		GlyphVector vetglifo = fntsis.createGlyphVector(inspetorfnt.getFontRenderContext(), frase);
		float tamfntredimens = (float) manipularTamFnt(inspetorfnt, vetglifo, vetglifo.getOutline(0, 0), imgaleat,
				fntsis);
		if (tamfntredimens != 0) {
			fntsis = fntsis.deriveFont(fntsis.getStyle(), tamfntredimens);
			inspetorfnt = grafimg.getFontMetrics(fntsis);
		}
		if (atrtxt != null)
			atrtxt.addAttribute(TextAttribute.FONT, fntsis);
		int[] postxt = new int[] { calcPosicHorizonLegend(imgaleat.getWidth(), inspetorfnt.stringWidth(frase)),
				LadoEditavelImg.BAIXO.toString().equals(ladovertescrita)
						? imgaleat.getHeight() - inspetorfnt.getAscent()
						: inspetorfnt.getAscent() * 2 };
		grafimg.setFont(fntsis);
		grafimg.setColor(Color.BLACK);
		if (atrtxt == null) {
			grafimg.drawString(frase, postxt[0], postxt[1]);
		} else {
			grafimg.drawString(atrtxt.getIterator(), postxt[0], postxt[1]);
		}
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