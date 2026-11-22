package br.edu.uniritter.memething;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;

public class Main {
	/*
	 * Regras de negócio: 1: Processar ao menos uma string do arquivo com frases de
	 * legenda e no máximo 100 MB do arquivo, caso contrário pular restantes 2:
	 * Processar ao menos uma imagem do caminho fornecido como argumento e no máximo
	 * uma pasta com 500 MB em imagens, caso contrário pular restantes 3: Receber no
	 * mínimo quatro argumentos conhecidos e no máximo seis argumentos conhecidos
	 */

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
		razaoerro = aferirComposicaoArg(args, COMPRIM_REAL);
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

	private static String aferirComposicaoArg(String[] lsarg, int comprim_real) throws IOException {
		boolean argtemarqfrase = false, argtemdirimg = false, argtemposicvert = false;

		for (int i = 1; i <= comprim_real; i += 2) {
			if (!argtemdirimg) {
				if (lsarg[(i-1)].equals("--imgdir") && new File(lsarg[i]).isDirectory()) {
					BufferedImage[] conjimg = validarDirImg(lsarg[i]);
					for(int j=0;j<conjimg.length;j++) {
						System.out.printf("\nOpacidade da imagem: %d\n",conjimg[j].getTransparency());
					}
				}
			}

			if (!argtemarqfrase) {
				if (lsarg[(i-1)].equals("--captionls") && new File(lsarg[i]).isFile()) {
					String[] conjlegend = validarArqFrase(lsarg[i]);
					System.out.printf("Strings de frases: %s.",Arrays.toString(conjlegend));
				}
			}

			if (!argtemposicvert) {
				if (lsarg[(i-1)].equals("--edge"))
					break; // chamar metodo para validar edge e se válido alterar argtemposicvert
			}
		}
		return "ok";
	}

	private static BufferedImage[] validarDirImg(String caminhodir) throws IOException {
		File dirimg = new File(caminhodir);
		File[] lsarq = dirimg.listFiles();
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
}