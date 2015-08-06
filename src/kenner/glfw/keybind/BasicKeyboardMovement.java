package kenner.glfw.keybind;

import static org.lwjgl.glfw.GLFW.*;
import kenner.ko.tools.SmdViewer;

/**
 * Keybinds:
 * WASD - Movement
 * Space - Fly Up
 * Left Shift - Fly Down
 * Left/Right - Rotate left/right
 * Up/Down - Rotate over pitch.  Buggy at the moment, wouldn't recommend doing much.
 * X - Toggle X-ray mode
 * ESC - Exit SMD Viewer
 */
public class BasicKeyboardMovement {
	
	private SmdViewer application;
	
	public BasicKeyboardMovement(SmdViewer app){
		application = app;
	}
	
	/**
	 * Keybinds for 1-press keys.
	 * @param window
	 * @param key
	 * @param scancode
	 * @param action
	 * @param mods
	 */
	public void glfwKeyCallback(long window, int key, int scancode, int action, int mods){
		//Exit on escape
		if(key == GLFW_KEY_ESCAPE && action != GLFW_RELEASE){
			application.end();
		}
		
		if(key == GLFW_KEY_X && action != GLFW_RELEASE){
			application.getXray().set(!application.getXray().get());
		}
	}
	
	/**
	 * Makes movement and rotation based on keys currently pressed
	 * @param distance
	 */
	public void move(float distance, float angle){
		boolean left = glfwGetKey(application.getWindowPtr(), GLFW_KEY_A) == GLFW_PRESS;
		boolean right = glfwGetKey(application.getWindowPtr(), GLFW_KEY_D) == GLFW_PRESS;
		boolean forward = glfwGetKey(application.getWindowPtr(), GLFW_KEY_W) == GLFW_PRESS;
		boolean backwards = glfwGetKey(application.getWindowPtr(), GLFW_KEY_S) == GLFW_PRESS;
		boolean up = glfwGetKey(application.getWindowPtr(), GLFW_KEY_SPACE) == GLFW_PRESS;
		boolean down = glfwGetKey(application.getWindowPtr(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;
		boolean yawRight = glfwGetKey(application.getWindowPtr(), GLFW_KEY_RIGHT) == GLFW_PRESS;
		boolean yawLeft = glfwGetKey(application.getWindowPtr(), GLFW_KEY_LEFT) == GLFW_PRESS;
		boolean pitchUp = glfwGetKey(application.getWindowPtr(), GLFW_KEY_UP) == GLFW_PRESS;
		boolean pitchDown = glfwGetKey(application.getWindowPtr(), GLFW_KEY_DOWN) == GLFW_PRESS;
		
		//figure out how many directions we are moving
		int directions = 0;
		if(left && !right || right && !left){
			directions++;
		}
		
		if(forward && !backwards || backwards && !forward){
			directions++;
		}
		
		if(up && !down || down && !up){
			directions++;
		}
		if(directions > 0){
			distance /= directions;
			application.setCameraMoved(true);
		
		
			//left/right movement
			if(left && !right){
				application.getCamera().moveLeft(distance);
			} else if(right && !left){
				application.getCamera().moveRight(distance);
			}
			
			//forward/backward movement
			if(forward && !backwards){
				application.getCamera().moveForward(distance);
			} else if(backwards && !forward){
				application.getCamera().moveBackward(distance);
			}
			
			//vertical movement
			if(up && !down){
				application.getCamera().moveUp(distance);
			} else if (down && !up){
				application.getCamera().moveDown(distance);
			}
		}
		
		//rotation calculation
		directions = 0;
		if(yawLeft && !yawRight || yawRight && !yawLeft){
			directions++;
		}
		if(pitchUp && !pitchDown || pitchDown && !pitchUp){
			directions++;
		}
		
		if(directions > 0){
			angle /= directions;
			application.setCameraMoved(true);
		
			if(yawLeft && !yawRight){
				application.getCamera().rotateLeft(angle);
			} else if(yawRight && !yawLeft){
				application.getCamera().rotateRight(angle);
			}
			
			if(pitchUp && !pitchDown){
				application.getCamera().rotateUp(angle);
			} else if(pitchDown && !pitchUp){
				application.getCamera().rotateDown(angle);
			}
		}
	}
}
