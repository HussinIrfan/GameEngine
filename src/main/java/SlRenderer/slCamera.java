package SlRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static csc133.spot.*;

public class slCamera {
    private Matrix4f projectionMatrix = new Matrix4f().identity();
    private Matrix4f veiwMatrix = new Matrix4f();
    private float f_left = 0.0f, f_right = (float)WIN_WIDTH, f_bottom = 0.0f,
            f_top = (float)WIN_HEIGHT, f_near = 0.0f, f_far = 10.0f;

    public Vector3f defaultLookFrom = new Vector3f(0f, 0f, 10f);
    public Vector3f defaultLookAt = new Vector3f(0f, 0f, -1.0f);
    public Vector3f defaultUpVector = new Vector3f(0f, 1.0f, 0f);

    private Vector3f curLookFrom = new Vector3f();
    private Vector3f curLookAt = new Vector3f();
    private Vector3f curUpVector = new Vector3f();

    private void setCamera(){};
    public slCamera(Vector3f camera_position){};
    public slCamera(){};
    public void setProjectionOrtho(){
        //takes previous set default arguments from the class
        projectionMatrix.setOrtho(f_left, f_right, f_bottom, f_top, f_near, f_far);
    };
    public void setProjectionOrtho(float left, float right, float bottom, float top, float near, float far){
        //saves them as default, then calls the function which takes the arguments from the defaults
        f_left = left;
        f_right = right;
        f_bottom = bottom;
        f_top = top;
        f_near = near;
        f_far = far;
        setProjectionOrtho();
    };
    public Matrix4f getViewMatrix(){
        veiwMatrix.identity();
        return veiwMatrix.lookAt(curLookFrom, curLookAt.add(defaultLookFrom), curUpVector);
    };

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
}