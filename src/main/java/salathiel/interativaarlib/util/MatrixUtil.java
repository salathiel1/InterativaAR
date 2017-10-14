package salathiel.interativaarlib.util;

/**
 * Created by salathiel on 23/09/17.
 */
public class MatrixUtil {


    public static float[][] identity(){
        float[][] MIdentity = new float[4][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++) {
                if(i == j) MIdentity[i][j] = 1;
                else MIdentity[i][j] = 0;
            }
        }
        return MIdentity;
    }


    public static float[][] multiply(float[][] firstarray, float[][] secondarray){
        float [][] result = new float[firstarray.length][secondarray[0].length];
        for (int i = 0; i < firstarray.length; i++) {
            for (int j = 0; j < secondarray[0].length; j++) {
                for (int k = 0; k < firstarray[0].length; k++) {
                    result[i][j] += firstarray[i][k] * secondarray[k][j];
                }
            }
        }
        return result;
    }

    public static float[] multiply(float[] vector, float[][] matrix) {
        float[] result = new float[4];
        for(int i = 0; i < 4; i++) result[i] = 0;

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                result[i] += vector[j] * matrix[i][j];
            }
        }

        return result;
    }

    public static float[][] vector2matrix(float[] v){
        float[][] matrix = new float[4][4];
        int k = 0;
        for(int i = 0; i< 4; i++){
            for(int j = 0; j < 4; j++){
                matrix[i][j] = v[k];
                k++;
            }
        }
        return matrix;
    }

    public static float[] matrix2vector(float[][] matrix){
        float[] vector = new float[16];
        int k = 0;
        for(int i = 0; i< 4; i++) {
            for (int j = 0; j < 4; j++) {
                vector[k] = matrix[i][j];
                k++;
            }
        }
        return vector;
    }

    public static float[][] transposeMatrix(float [][] m){
        float[][] temp = new float[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    public static float[] transposeVector(float [] v){
        float[][] m = vector2matrix(v);
        return matrix2vector(transposeMatrix(m));
    }

    public static float[] normalize(float[] pos){
        float[] normal = new float[4];
        for(int i = 0; i < 4; i++)
            normal[i] = pos[i] / pos[3];
        return normal;
    }

    public static float[] convert2d(float[] pos, float[][] transformationMatrix, float[][] projectionMatrix, int screenWidth, int screenHeight)
    {
        float[][] mpro = projectionMatrix;
        float[][] mtra = transformationMatrix;
        pos = multiply(pos, mtra);
        pos = multiply(pos, mpro);
        pos = normalize(pos);
        pos[0] = (float) (screenWidth * (pos[0] + 1.0) / 2.0);
        pos[1] = (float) (screenHeight * (1.0 - ((pos[1] + 1.0) / 2.0)));
        return pos;
    }
}
