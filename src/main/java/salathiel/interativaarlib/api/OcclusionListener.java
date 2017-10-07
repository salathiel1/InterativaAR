package salathiel.interativaarlib.api;

public interface OcclusionListener {

	//funcao chamada quando ocorre a oclusao de um marcador que ja esteve visivel
	public void occlusion(int intensity);
}
