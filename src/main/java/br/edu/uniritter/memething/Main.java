package br.edu.uniritter.memething;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main {
	enum OpcaoValidaMenu {
		CRIAR_MEME, DEFINIR_FNT_NA_MEM, DEFINIR_COR_FNT_NA_MEM, ENCERRAR_PROG, INVALIDA
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

	protected static class AtrPersonalizado extends Attribute {
		private static final long serialVersionUID = 1L;
		private static final AtrPersonalizado COR_FNT = new AtrPersonalizado("corfonte");

		protected AtrPersonalizado(String corfnt) {
			super(corfnt);
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
	 * Regras de negócio: 1: Processar ao menos uma string (>1 byte) do arquivo com
	 * frases de legenda e no máximo 32 KB do arquivo, caso contrário pular
	 * restantes 2: Processar uma imagem com no mínimo 8 bytes (nenhum arquivo PNG
	 * com menos bytes do que sua assinatura mágica pode ser uma imagem válida) e no
	 * máximo com 5000 KB, caso contrário pulá-la 3: Receber no mínimo quatro
	 * argumentos conhecidos e no máximo sete argumentos conhecidos; ignorar
	 * argumentos desconhecidos
	 */

	private static final String CAPTION_LS_FLAG = "--captionls", IMG_DIR_FLAG = "--imgdir", EDITED_EDGE_FLAG = "--edge",
			REVIEW_MODE_FLAG = "--reviewtime", HELP_FLAG = "--help", DELIMIT_FRASE = ";", FNT_PADRAO = "SansSerif",
			COR_FNT_PADRAO = "black";
	private static final int COMPRIM_RECUSADO_VET_STR = 0, COMPRIM_MIN_VET_IMG = 1, COMPRIM_MIN_ARG = 4,
			COMPRIM_MAX_ARG = 7;
	private static final double FATOR_MARG = 1.05;
	private static final short TAM_FNT_PADRAO = 100;
	private static final BigDecimal TAM_MAX_KB_ARQ_IMG = new BigDecimal(5000),
			TAM_MIN_KB_ARQ_IMG = new BigDecimal("0.008"), TAM_MAX_KB_ARQ_STR = new BigDecimal(Integer.SIZE),
			TAM_MIN_KB_ARQ_STR = new BigDecimal("0.002"), FATR_B_CONVERSAO = new BigDecimal(1000);
	private static final SecureRandom aleatSegur = new SecureRandom();
	private static OperacaoComMeme conjArtefato;

	public static void main(String[] args)
			throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		final int comprimReal = args.length;
		execOperacionalizacao(COMPRIM_MIN_ARG, COMPRIM_MAX_ARG, comprimReal, args);
	}

	private static void execOperacionalizacao(int comprimMinArg, int comprimMaxArg, int comprimReal, String[] args)
			throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (Arrays.asList(args).contains(HELP_FLAG)) {
			System.out.printf(
					"===Ajuda do programa===\nAbaixo, colchetes denotam um parâmetro facultativo:\n\n--imgdir <caminho>\tpasta que contém as imagens de amostra, uma sorteada por meme\n--captionls <caminho>\tarquivo com strings para a legenda separadas por ponto e vírgula\n[--edge <top|bottom>]\to lado vertical próximo do qual a legenda deve ser inserida\n[--reviewtime]\tperguntar se o valor sorteado deve ser descartado ou usado\n[--help]\texibir esta mensagem de ajuda e encerrar");
			return;
		}
		Scanner escaneadr = (System.console() != null) ? new Scanner(System.in, System.console().charset())
				: new Scanner(System.in);
		String razaoErro = aferirIntervaloComprimento(comprimMinArg, comprimMaxArg, comprimReal,
				Arrays.asList(args).contains(REVIEW_MODE_FLAG));
		if (razaoErro != null) {
			System.out.printf("[ERRO FATAL] %s\n", razaoErro);
			return;
		}
		conjArtefato = manipularComposicaoArg(args, comprimReal);
		byte selecaoEscolhaMenu = 0;
		AttributedString txtcomatr = new AttributedString(new String("-"));
		if (conjArtefato == null)
			return;
		while (selecaoEscolhaMenu != -1) {
			selecaoEscolhaMenu = encaminharOpcaoMenu(escaneadr, txtcomatr);
		}
		escaneadr.close();
	}

	static byte encaminharOpcaoMenu(Scanner escaneadr, AttributedString txtComAtr)
			throws IOException, IllegalAccessException {
		System.out.printf(
				"As operações disponíveis neste programa são as seguintes:\n\n1 — Criar meme com as configurações atuais (será salvo em <pasta de seu usuário>/.memething/editado)\n2 — Definir fonte tipográfica para os próximos memes (até o programa sair)\n3 — Definir cor da fonte tipográfica para os próximos memes (até o programa sair)\n4 — Encerrar programa\n\nDigite o código (1–4) correspondente à opção desejada: ");
		byte selecaoMenu = 0;
		while (!escaneadr.hasNextByte()) {
			if (!escaneadr.hasNextLine()) {
				return -1; // evita erro caso a entrada seja encerrada (Ctrl+Z / Ctrl+D)
			}
			System.out.printf("Opção inválida! Digite um número inteiro de 1 a 4 correspondente à devida opção: ");
			escaneadr.nextLine();
		}
		selecaoMenu = escaneadr.nextByte();
		escaneadr.nextLine();
		selecaoMenu--;
		OpcaoValidaMenu[] conjOpc = OpcaoValidaMenu.values();
		OpcaoValidaMenu opcao = (selecaoMenu >= 0 && selecaoMenu < (conjOpc.length - 1)) ? conjOpc[selecaoMenu]
				: OpcaoValidaMenu.INVALIDA;
		switch (opcao) {
		case CRIAR_MEME -> {
			Map<Attribute, Object> conjatr = txtComAtr.getIterator().getAttributes();
			Object familiafntusuario = conjatr.get(TextAttribute.FAMILY),
					corfntusuario = conjatr.get(AtrPersonalizado.COR_FNT);
			System.out.printf(
					"===CONFIGURAÇÕES ATUAIS===\nFonte tipográfica: %s\nCor da fonte da legenda (em inglês): %s.\n",
					familiafntusuario != null ? familiafntusuario.toString()
							: "padrão do programa (".concat(FNT_PADRAO).concat(")"),
					corfntusuario != null ? corfntusuario.toString()
							: "padrão do programa (".concat(COR_FNT_PADRAO).concat(")"));
			BufferedImage imgaleat = obterImgAleatoria(conjArtefato.getConjimg(), conjArtefato.getLsarq(),
					conjArtefato.temRevisaoativ(), escaneadr);
			String frasesel = obterFraseAleatoria(conjArtefato.getConjlegend(), conjArtefato.temRevisaoativ(),
					escaneadr);
			txtComAtr = new AttributedString(frasesel);
			txtComAtr.addAttributes(conjatr, 0, frasesel.length());
			Graphics ctxgrafimg = desenharStringEmImg(imgaleat, frasesel, conjArtefato.getLadoedicaovert(),
					!txtComAtr.getIterator().getAllAttributeKeys().isEmpty() ? txtComAtr : null);
			String seloAtualDtHr = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss").withZone(ZoneId.systemDefault())
					.format(Instant.now());
			String prefixDirEscrita = System.getProperty("user.home");
			File arqParaEscrita = new File(prefixDirEscrita,
					"memething/editado/meme_".concat(seloAtualDtHr).concat(".png"));
			boolean arqEstaDisponivel = manipularArqSaida(arqParaEscrita);
			if (arqEstaDisponivel) {
				gravarImgEmArq(imgaleat, arqParaEscrita);
			} else
				return -1;
			ctxgrafimg.dispose();
		}
		case DEFINIR_FNT_NA_MEM -> {
			System.out
					.printf("Insira ao lado o nome da fonte tipográfica para a legenda do meme (por exemplo, Arial): ");
			String nomefamiliafnt = escaneadr.nextLine();
			boolean fntdisponivel = aferirDisponibilidadeFnt(nomefamiliafnt);
			if (!fntdisponivel) {
				System.out.printf("A fonte que você informou não pôde ser carregada de seu sistema.\n");
			} else
				txtComAtr.addAttribute(TextAttribute.FAMILY, nomefamiliafnt);
		}
		case DEFINIR_COR_FNT_NA_MEM -> {
			System.out.printf(
					"Insira ao lado a cor EM INGLÊS da fonte tipográfica para a legenda do meme (por exemplo, \"red\"): ");
			String nomecorfnt = escaneadr.nextLine();
			Color cordisponivel = procurarFntLocal(nomecorfnt);
			if (cordisponivel == null)
				System.out.printf(
						"A cor que você informou não pôde ser localizada. Certifique-se de escrever a cor em inglês.\n");
			else {
				txtComAtr.addAttribute(AtrPersonalizado.COR_FNT, nomecorfnt);
				txtComAtr.addAttribute(TextAttribute.FOREGROUND, cordisponivel);
			}
		}
		case ENCERRAR_PROG -> {
			System.out.printf("Encerramento solicitado.");
			return -1;
		}
		default -> {
			System.out.printf("Opção inválida! Listando operações novamente...\n");
			return -2;
		}
		}
		return selecaoMenu;
	}

	static String aferirIntervaloComprimento(int comprimMin, int comprimMax, int comprimReal, boolean temrevisao) {
		boolean comprimincompleto = temrevisao ? (comprimReal == 4 || comprimReal == 6)
				: (comprimReal == 5 || comprimReal == 7);
		String razaoretornocomp = comprimReal < comprimMin
				? "lista de argumentos insuficiente (mínimo são ".concat(String.valueOf(comprimMin)).concat(").")
				: (comprimReal > comprimMax
						? "lista de argumentos excessiva (máximo são ".concat(String.valueOf(comprimMax)).concat(").")
						: (comprimincompleto
								? "lista de argumentos está incompleta; argumentos esperados: 4 ou 6, sem --reviewtime; OU 5 a 7, INCLUINDO --reviewtime"
								: null));
		return razaoretornocomp;
	}

	static OperacaoComMeme manipularComposicaoArg(String[] lsarg, int comprimReal) throws IOException {
		boolean argtemarqfrase = false, argtemdirimg = false, ladoedicaoestadef = false, conjargestasuficiente = false;
		BufferedImage[] conjimg = new BufferedImage[0];
		String[] conjlegend = new String[0];
		String ladoEdicaoVert = new String();
		OperacaoComMeme conjArtefRetornado;
		if (Arrays.asList(lsarg).contains(REVIEW_MODE_FLAG)) {
			String[] lsargaux = new String[0];
			int controladrpos = -1;
			for (int i = 0; i < lsarg.length; i++) {
				if (lsarg[i].equals(REVIEW_MODE_FLAG)) {
					continue;
				} else {
					lsargaux = controladrpos == -1 ? new String[1] : Arrays.copyOf(lsargaux, (lsargaux.length + 1));
					controladrpos++;
					lsargaux[controladrpos] = lsarg[i];
				}
			}
			lsarg = lsargaux;
			comprimReal = lsarg.length;
			conjArtefRetornado = new OperacaoComMeme(true);
		} else {
			conjArtefRetornado = new OperacaoComMeme(false);
		}
		File[] lsarq = new File[0], lsarqutil = new File[0];
		for (int i = 1; i < comprimReal; i += 2) {
			if (!argtemdirimg) {
				if (lsarg[(i - 1)].equals(IMG_DIR_FLAG)) {
					boolean temdir = new File(lsarg[i]).isDirectory();
					if (temdir) {
						File dirimg = new File(lsarg[i]);
						lsarq = aferirItemFilho(dirimg);
						if (lsarq == null) {
							break;
						} else if (lsarq.length == 0) {
							System.out.printf(
									"[ERRO FATAL] Não existe nenhum arquivo elegível na pasta informada. É necessário pelo menos um arquivo que seja uma imagem.");
							break;
						}
						BigDecimal[][] lsprop = new BigDecimal[0][0];
						int indProp;
						for (int j = 0; j < lsarq.length; j++) {
							BigDecimal resultConvTam = converterValBParaKB(lsarq[j].length(), j);
							if (aferirAceitacaoBigDecParaImg(resultConvTam, true) == -1) {
								System.out.printf(
										"[AVISO] O arquivo %s não satisfaz o tamanho mínimo de imagem individual processado por este programa (%s KB = 8 B). Ele não será sorteado como base de meme.\n\n",
										lsarq[j].getAbsolutePath(), TAM_MIN_KB_ARQ_IMG.toPlainString());
								continue;
							} else if (aferirAceitacaoBigDecParaImg(resultConvTam, false) == 1) {
								System.out.printf(
										"[AVISO] O arquivo %s excede o tamanho máximo de imagem individual processado por este programa (%s KB). Ele não será sorteado como base de meme.\n\n",
										lsarq[j].getAbsolutePath(), TAM_MAX_KB_ARQ_IMG.toPlainString());
								continue;
							} else {
								BufferedImage imgLida = ImageIO.read(lsarq[j]);
								if (imgLida != null) {
									conjimg = preencherOuConcatVet(conjimg, imgLida);

									lsarqutil = preencherOuConcatVet(lsarq, lsarqutil, j);
									if (lsprop.length == 0) {
										lsprop = new BigDecimal[1][3];
										indProp = (lsprop.length - 1);
										lsprop[indProp][0] = resultConvTam;
										lsprop[indProp][1] = BigDecimal.valueOf(imgLida.getWidth());
										lsprop[indProp][2] = BigDecimal.valueOf(imgLida.getHeight());
									} else {
										lsprop = Arrays.copyOf(lsprop, lsprop.length + 1);
										lsprop[lsprop.length - 1] = new BigDecimal[] { resultConvTam,
												BigDecimal.valueOf(imgLida.getWidth()),
												BigDecimal.valueOf(imgLida.getHeight()) };
									}
									System.out.printf("Propriedades da imagem %s — tamanho, largura, altura:\n",
											lsarq[j].getAbsolutePath());
									String[] unid = new String[] { "KB", "px" };
									System.out.printf("%s %s", lsprop[j][0].toPlainString(), unid[0]);
									for (int k = 1; k <= 2; k++) {
										System.out.printf(", %s %s", lsprop[j][k].toPlainString(), unid[1]);
									}
									System.out.printf(".\n\n");
								}
							}
						}
						System.out.printf("\n");
						for (short j = 0; j < conjimg.length; j++) {
							System.out.printf("%s carregado com sucesso.\n", lsarqutil[j].getAbsolutePath());
						}
						if (conjimg.length >= COMPRIM_MIN_VET_IMG)
							argtemdirimg = true;
						else {
							System.out.printf("[ERRO FATAL] Diretório não contém imagens.");
							break;
						}
					} else {
						System.out.printf("[ERRO FATAL] Pasta informada não existe.");
						break;
					}
				}
			}

			if (!argtemarqfrase) {
				if (lsarg[(i - 1)].equals(CAPTION_LS_FLAG)) {
					if (new File(lsarg[i]).isFile()) {
						conjlegend = validarArqFrase(lsarg[i]);
						if (conjlegend == null || conjlegend[0] == null)
							break;
						System.out.printf("\nStrings de frases: %s\n\n", resgatarStringDeArrComPontuacao(conjlegend));
						argtemarqfrase = true;
					} else {
						System.out.printf("[ERRO FATAL] Arquivo de frases informado não existe.");
						break;
					}
				}
			}

			if (!ladoedicaoestadef) {
				if (lsarg[(i - 1)].equals(EDITED_EDGE_FLAG)) {
					ladoEdicaoVert = lsarg[i].equals(LadoEditavelImg.BAIXO.toString())
							? LadoEditavelImg.BAIXO.toString()
							: LadoEditavelImg.TOPO.toString();
					ladoedicaoestadef = true;
				}
			}
			if (argtemdirimg && argtemarqfrase) {
				conjArtefRetornado.setLsarq(lsarqutil);
				conjArtefRetornado.setConjimg(conjimg);
				conjArtefRetornado.setConjlegend(conjlegend);

				if (ladoedicaoestadef) {
					conjArtefRetornado.setLadoedicaovert(ladoEdicaoVert);
					return conjArtefRetornado;
				}
				conjargestasuficiente = true;
			}
		}
		if (conjargestasuficiente)
			conjArtefRetornado.setLadoedicaovert(LadoEditavelImg.TOPO.toString());
		else {
			System.out
					.printf("[ERRO FATAL] Argumentos necessários ausentes; pelo menos um desconhecido foi informado.");
			conjArtefRetornado = null;
		}
		return conjArtefRetornado;
	}

	static int aferirAceitacaoBigDecParaImg(BigDecimal resultConvTam, boolean primInvocIteracao) {
		return primInvocIteracao ? resultConvTam.compareTo(TAM_MIN_KB_ARQ_IMG)
				: resultConvTam.compareTo(TAM_MAX_KB_ARQ_IMG);
	}

	static BigDecimal converterValBParaKB(long compLsarq, int j) {
		BigDecimal resultConvTam = new BigDecimal(compLsarq).divide(FATR_B_CONVERSAO);
		return resultConvTam;
	}

	static File[] aferirItemFilho(File dirimg) {
		File ancestral = dirimg;
		while ((ancestral = ancestral.getParentFile()) != null && !ancestral.exists()) {
		}
		boolean temPermissao = (ancestral != null && Files.isWritable(ancestral.toPath()));
		if (!temPermissao) {
			System.out.printf("[ERRO FATAL] O diretório-pai %s não é passível de escrita.",
					ancestral.getAbsolutePath());
			return null;
		}
		File[] conjArqValid = new File[0], conjArqValidAux = dirimg.listFiles((dir, name) -> !name.equals("out"));
		File arq;
		for (int i = 0; i < conjArqValidAux.length; i++) {
			arq = conjArqValidAux[i];
			if (arq.isFile()) {
				conjArqValid = conjArqValid.length == 0 ? new File[1]
						: Arrays.copyOf(conjArqValid, (conjArqValid.length + 1));
				conjArqValid[(conjArqValid.length - 1)] = arq;
			}
		}
		return conjArqValid;
	}

	static BufferedImage[] preencherOuConcatVet(BufferedImage[] conjimg, BufferedImage imgLida) {
		conjimg = conjimg.length == 0 ? new BufferedImage[1] : Arrays.copyOf(conjimg, (conjimg.length + 1));
		conjimg[(conjimg.length - 1)] = imgLida;
		return conjimg;
	}

	static File[] preencherOuConcatVet(File[] lsarq, File[] lsarqutil, int pos) {
		lsarqutil = lsarqutil.length == 0 ? new File[1] : Arrays.copyOf(lsarqutil, (lsarqutil.length + 1));
		lsarqutil[(lsarqutil.length - 1)] = lsarq[pos];
		return lsarqutil;
	}

	static String[] validarArqFrase(String caminhoarq) throws IOException {
		File arqlegend = new File(caminhoarq);
		String[] conjlegend = null;
		boolean temcorte = false;
		if (aferirAceitacaoBigDecParaLeg(new BigDecimal((arqlegend.length())).divide(FATR_B_CONVERSAO), true) == -1) {
			System.out.printf("[ERRO FATAL] O arquivo com legendas para o meme praticamente não possui texto.\n");
			return new String[] { null };
		} else if (aferirAceitacaoBigDecParaLeg(new BigDecimal((arqlegend.length())).divide(FATR_B_CONVERSAO),
				false) == 1) {
			System.out.printf(
					"[AVISO] O arquivo com legendas para o meme possui um tamanho superior ao permitido. Ele será reduzido ao maior tamanho compatível por meio da exclusão das últimas frases.\n");
			temcorte = true;
		}
		int limiteB = TAM_MAX_KB_ARQ_STR.intValue() * FATR_B_CONVERSAO.intValue();
		byte[] leituraB;
		try (FileInputStream streamEntrada = new FileInputStream(arqlegend)) {
			leituraB = temcorte ? streamEntrada.readNBytes(limiteB) : streamEntrada.readAllBytes();
		}
		String texto = new String(leituraB, StandardCharsets.UTF_8).replace("\r", new String()).replace("\n",
				new String());
		int ultimaDelimit = texto.lastIndexOf(DELIMIT_FRASE);
		if (ultimaDelimit == -1) {
			System.out.printf("[ERRO FATAL] Não foi encontrado o separador \";\" para indexar as frases.\n");
			return null;
		}
		if (temcorte)
			texto = texto.substring(0, ultimaDelimit); // descarta a frase final cortada no limite de 32 KB
		String[] segmentacaoBruta = texto.split(DELIMIT_FRASE);
		for (int i = 0; i < segmentacaoBruta.length; i++) {
			String fraseLimpa = segmentacaoBruta[i].contains("\u0020") ? segmentacaoBruta[i].trim()
					: segmentacaoBruta[i];
			if (!fraseLimpa.isEmpty()) {
				conjlegend = conjlegend.length == 0 ? new String[0] : Arrays.copyOf(conjlegend, conjlegend.length + 1);
				conjlegend[(conjlegend.length - 1)] = fraseLimpa;
			}
		}
		/*
		 * if (temcorte) { char[] bufferLimitd = new char[(TAM_MAX_KB_ARQ_STR.intValue()
		 * * FATR_B_CONVERSAO.intValue())]; int leiturab =
		 * leitorsegmnt.read(bufferLimitd, 0, bufferLimitd.length); String legendtrunc =
		 * new String(bufferLimitd, 0, leiturab); int ultimaDelimit =
		 * legendtrunc.lastIndexOf(DELIMIT_FRASE); if (ultimaDelimit != -1) {
		 * legendtrunc = legendtrunc.substring(0, ultimaDelimit); } while ((ind =
		 * legendtrunc.indexOf(DELIMIT_FRASE, ind)) != -1) { ctdrocorrencia++; ind +=
		 * DELIMIT_FRASE.length(); } if (ctdrocorrencia == COMPRIM_RECUSADO_VET_STR) {
		 * System.out.
		 * printf("[ERRO FATAL] Não foi encontrado o separador \";\" para indexar as frases."
		 * ); leitorarq.close(); return null; } conjlegend =
		 * legendtrunc.split(DELIMIT_FRASE); } else { String linhautil = new String(),
		 * linhasincroniz = null; while (linhautil != null) { linhasincroniz =
		 * linhasincroniz == null ? new String() : linhasincroniz.concat(linhautil);
		 * linhautil = leitorsegmnt.readLine(); } while ((ind =
		 * linhasincroniz.indexOf(DELIMIT_FRASE, ind)) != -1) { ctdrocorrencia++; ind +=
		 * DELIMIT_FRASE.length(); } if (ctdrocorrencia == COMPRIM_RECUSADO_VET_STR) {
		 * System.out.
		 * printf("[ERRO FATAL] Não foi encontrado o separador \";\" para indexar as frases."
		 * ); leitorarq.close(); return null; } conjlegend =
		 * linhasincroniz.split(DELIMIT_FRASE); }
		 */
		if (conjlegend.length == COMPRIM_RECUSADO_VET_STR)
			return null;
		return conjlegend;
	}

	private static int aferirAceitacaoBigDecParaLeg(BigDecimal resultConvTam, boolean primInvocIteracao) {
		return primInvocIteracao ? resultConvTam.compareTo(TAM_MIN_KB_ARQ_STR)
				: resultConvTam.compareTo(TAM_MAX_KB_ARQ_STR);
	}

	static BufferedImage obterImgAleatoria(BufferedImage[] conjamostraimg, File[] lsarq, boolean revisaoativ,
			Scanner escaneadr) {
		int aleatsegur;
		for (;;) {
			aleatsegur = obterIntAleatSegur(conjamostraimg.length);
			if (revisaoativ) {
				System.out.printf("A imagem aleatória selecionada está no caminho "
						.concat(lsarq[aleatsegur].getAbsolutePath())
						.concat(". Você deseja continuar com ela? Digite \"não\" se não quiser, caso contrário \"sim\" ou qualquer outro valor: "));
				String decisaorev = escaneadr.nextLine();
				decisaorev = decisaorev.toLowerCase(Locale.ROOT);
				if (decisaorev.contains("\u0020")) {
					decisaorev = decisaorev.trim();
				}
				if (!decisaorev.equals("não") && !decisaorev.equals("nao"))
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

	static String obterFraseAleatoria(String[] conjlegend, boolean revisaoAtiv, Scanner escaneadr) {
		int indAleatSegur;
		for (;;) {
			indAleatSegur = obterIntAleatSegur(conjlegend.length);
			if (revisaoAtiv) {
				System.out.printf("A frase aleatória selecionada é ".concat(conjlegend[indAleatSegur]).concat(
						". Você deseja continuar com ela? Digite \"não\" se não quiser, caso contrário \"sim\" ou qualquer outro valor: "));
				String decisaorev = escaneadr.nextLine();
				if (!decisaorev.toLowerCase().equals("não"))
					break;
			} else
				break;
		}
		return conjlegend[indAleatSegur];
	}

	static int obterIntAleatSegur(int comprim) {
		return aleatSegur.nextInt(comprim);
	}

	static boolean aferirDisponibilidadeFnt(String nomefamiliafnt) {
		String[] bibfnt = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		return Arrays.asList(bibfnt).contains(nomefamiliafnt);
	}

	static Color procurarFntLocal(String nomecorfnt) throws IllegalAccessException {
		Color corFnt;
		try {
			corFnt = Color.class.cast(Color.class.getField(nomecorfnt.trim().toUpperCase()).get(null));
		} catch (NoSuchFieldException nsfe) {
			corFnt = null;
		}
		return corFnt;
	}

	static Graphics desenharStringEmImg(BufferedImage imgAleat, String frase, String ladoVertEscrita,
			AttributedString atrTxt) {
		Object atrfamfnt = atrTxt != null ? atrTxt.getIterator().getAttribute(TextAttribute.FAMILY)
				: (Object) FNT_PADRAO,
				atrcorfnt = atrTxt != null ? atrTxt.getIterator().getAttribute(TextAttribute.FOREGROUND) : Color.BLACK;
		Font fntSis = atrfamfnt != null ? new Font(atrfamfnt.toString(), Font.BOLD, TAM_FNT_PADRAO)
				: new Font(FNT_PADRAO, Font.BOLD, TAM_FNT_PADRAO);
		if (atrcorfnt == null)
			atrTxt.addAttribute(TextAttribute.FOREGROUND, Color.BLACK);
		Graphics grafImg = imgAleat.getGraphics();
		FontMetrics inspetorFnt = grafImg.getFontMetrics(fntSis);
		GlyphVector vetGlifo = fntSis.createGlyphVector(inspetorFnt.getFontRenderContext(), frase);
		float tamFntRedimens = (float) manipularTamFnt(vetGlifo.getOutline(0, 0), imgAleat, fntSis);
		if (tamFntRedimens != 0) {
			fntSis = fntSis.deriveFont(fntSis.getStyle(), tamFntRedimens);
			inspetorFnt = grafImg.getFontMetrics(fntSis);
		}
		if (atrTxt != null)
			atrTxt.addAttribute(TextAttribute.FONT, fntSis);
		int margVert = (int) (imgAleat.getHeight() * (FATOR_MARG - 1.0));
		int[] posTxt = new int[] { calcPosicHorizonLegend(imgAleat.getWidth(), inspetorFnt.stringWidth(frase)),
				LadoEditavelImg.BAIXO.toString().equals(ladoVertEscrita)
						? Math.max(inspetorFnt.getAscent(), imgAleat.getHeight() - inspetorFnt.getDescent() - margVert)
						: Math.min(margVert + inspetorFnt.getAscent(),
								imgAleat.getHeight() - inspetorFnt.getDescent()) };
		grafImg.setFont(fntSis);
		grafImg.setColor(Color.BLACK);
		if (atrTxt == null) {
			grafImg.drawString(frase, posTxt[0], posTxt[1]);
		} else {
			grafImg.drawString(atrTxt.getIterator(), posTxt[0], posTxt[1]);
		}
		return grafImg;
	}

	private static boolean manipularArqSaida(File arqSaida) {
		if (!arqSaida.exists() && !arqSaida.getParentFile().exists()) {
			System.out.printf(
					"[AVISO] A criação de um ou mais diretórios se faz necessária. O programa vai tentar criá-los agora.\n");
			if (!arqSaida.getParentFile().mkdirs()) {
				System.out.printf(
						"[ERRO FATAL] O programa não conseguiu criar os diretórios necessários para armazenar os memes. Certifique-se de que o usuário com o qual você está logado tem permissão RW para eles.\n");
				return false;
			} else
				System.out.printf(
						"[AVISO] O programa criou os diretórios necessários para armazenar o arquivo com sucesso.\n");
		}
		return true;
	}

	private static void gravarImgEmArq(BufferedImage imgMeme, File arqAGravar) throws IOException {
		System.out
				.printf(ImageIO.write(imgMeme, "png", arqAGravar) != false ? "\nUm novo meme foi gravado com sucesso.\n"
						: ("\nNão foi possível escrever a imagem no arquivo em "
								.concat(arqAGravar.getAbsolutePath().concat(".\n"))));
	}

	static double manipularTamFnt(Shape contorno, BufferedImage imgamostra, Font fntsis) {
		double largtxtnormal = contorno.getBounds().getWidth(), alturatxtnormal = contorno.getBounds().getHeight();
		int largimg = imgamostra.getWidth();
		int alturaimg = imgamostra.getHeight();
		boolean margemconfortavel = largimg > largtxtnormal * FATOR_MARG && alturaimg > alturatxtnormal * FATOR_MARG;
		if (margemconfortavel)
			return 0;
		else {
			double tamfntreflarg = (fntsis.getSize2D() * largimg) / largtxtnormal,
					tamfntrefaltura = (fntsis.getSize2D() * alturaimg) / alturatxtnormal;
			return (tamfntreflarg < tamfntrefaltura) ? tamfntreflarg : tamfntrefaltura;
		}
	}

	static int calcPosicHorizonLegend(int largimg, int largtxt) {
		return new BigDecimal(largimg).subtract(new BigDecimal(largtxt)).divide(BigDecimal.TWO, RoundingMode.HALF_UP)
				.intValue();
	}

	static String resgatarStringDeArrComPontuacao(String[] conjlegend) {
		if (conjlegend == null || (conjlegend.length - 1) == -1)
			return "NENHUMA";
		int iMax = conjlegend.length;
		StringBuilder constrstr = new StringBuilder();
		for (int i = 0; i < iMax; i++) {
			constrstr.append(String.valueOf(conjlegend[i]));
			constrstr.append(i == (iMax - 1) ? "." : "; ");
		}
		return constrstr.toString();
	}
}