# memething
Memething é um app Java que cria um meme a partir do caminho de um arquivo contendo legendas e do caminho de um diretório com imagens template, selecionando aleatoriamente um de cada tipo e mesclando-os. Por padrão, a legenda é rasterizada no topo da imagem, sendo possível alternar para o lado inferior com a flag `--edge`.

Uso:  
`memething [--imgdir <caminho para pasta com imagens>] [--caption <caminho para arquivo .csv com legendas>] [--edge <top|bottom>] `