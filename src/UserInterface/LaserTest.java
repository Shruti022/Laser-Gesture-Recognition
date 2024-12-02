/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserInterface;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
/*import org.opencv.imgcodecs.Imgcodecs;*/
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import utilities.ImageProcessor;


//System.setProperty("java.library.path", "D:\\SEM6\\opencvpro\\opencv\\build\\java");
//System.loadLibrary("opencv_java300");

/**
 *
 * This is the main class the captures the image from webcam, detects the laser pointer and processes the 
 * gesture.
 * The basic logic for processing the gesture is pretty simple.
 * We simply calculate the the displacement of the laser point of both X-Axis and Y-Axis.
 * Then we compare the which displacement is greater X or Y.
 * Then further we check whether the displacement is greater than the minimum threshold value.
 * Then finally we check if the displacement if +ve or -ve to check which gesture is it.S
 * 
 * For understanding further details about this process refer the code below.
 */
public class LaserTest extends javax.swing.JFrame {

    /**
     * Creates new form LaserTest
     * Creates a new Thread that captures the images from webcam and processes them and then recognizes the 
     * gesture.
     */
    public LaserTest() {
        initComponents();
        customInit();   
    }

    /**
    * This method basically initializes the native OpenCV library.
    * Then it starts the thread for capturing images from webcam.
    * Also initializes the object of robot that would generate the events when gesture is recognized.
    **/
    private void customInit() {
    	//Loading the native library
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        //Starting the image capturing thread.
        new Thread() {
            @Override
            public void run() {
                video();
            }
        }.start();

        //Initializing the robot Object to generate events;
    	try {
            robot = new Robot();
        } catch(AWTException awte) {
            System.out.println("AWT Exception caught : " + awte.getMessage());
        } catch(Exception e) {
            System.out.println("Exception caught : " + e.getMessage());
        }
    }

    /**
    * This method is invoked by the thread created in the customInit Method.
    * It capture the image from the webcam using VideoCapture Class with a resolution of 1366,768.
    * Then detects the laser-pointer,calculates the co-ordinates,takes the displacement and recognizes
    * the gesture created by the pointer.
	* 
	******************************************************************************************************
	*	LASER DETECTION LOGIC
	*	1) First we take the captured image and then apply a threshold value to it using threshold method of
	* 		Imgproc class of OpenCV.This threshold returns a image that has the objects with bright colors.
	* 		Then we apply the inRange method to take create a mask of the objects with red color.
	*******************************************************************************************************
	*       GESTURE RECOGNITION LOGIC
	* 	We have used 2 flags in this process
	* 		-isAnchorPoint = used to indicate if there is an anchor point present from th previous images.
	* 				Anchor point is nothing but a point from which we calculate the displacement
	* 						 of the laser pointer.
	* 		-gestureRecognize = used to indicate that a gesture Has been recognized and no further displacement 
	* 				should be calculated unless and until a new anchorPoint is found.
	*   1) First we try to get Location of the pointer on the current image.If the co-ordinates are found the we
	*   process further otherwise we set anchorPoint and gestureRecognized to false.
	*   2) If the co-ordinates are found then we check if there is a anchor point present or not if it is not 
	*   present then we set the current position as anchorPoint co-ordinates and set anchorPoint to true.
	*	   If the anchor point is found then we proceed to Step-3.
	*   3) We calculate the X, Y displacement by subtracting the current co-ordinates and anchorPoint co-ordinates
	* 	Then we check which displacement is greater(X/Y);
	*   4) Then we check whether the displacement is +ve or -ve and perform the respective operations when gesture
	*      is recognized.
    **/
    private void video() { 
    	//Intitializing videoCapture object that would capture the images from default in-built webcam.   	
        VideoCapture capture = new VideoCapture(0);
        
        //Initliaizing the matrix in which the images captured from webcam would be stored.
        Mat webcamMatImage = new Mat();
        
        //Setting the resoultion of the webcam.
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,1366);
	capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,768);

        Mat mat = new Mat(3,3,CvType.CV_8U,new Scalar(1,1,1));
        
        if( capture.isOpened()){  
            while (true){  
                capture.read(webcamMatImage);  
                if( !webcamMatImage.empty() ){  
                    Mat inRange = new Mat();
                    Mat temp = new Mat();
                    //Converting the image to the provided threshold value.
                    Imgproc.threshold(webcamMatImage,inRange, 150, 255, Imgproc.THRESH_BINARY);
                    //Extracting only red color objects from the image.
                    Core.inRange(inRange, new Scalar(0,0,250), new Scalar(0,0,255), temp);
                    //Checking if laser is present or not in the image.
                    if(getX(temp) != -1) {
                        //Checking if anchorPoint is present or not.
                        if(!isAnchorPoint || gestureDetected) {
                                //Setting the anchor points
                            this.anchorX = getX(temp);
                            this.anchorY = getY(temp);

                            this.isAnchorPoint = true;
                        }
                        else {
                            //Calculating the x and y displacement.
                            int x = getX(temp) - this.anchorX;
                            int y = getY(temp) - this.anchorY;
                            //Checking which if X displcement is greater or Y.
                            if((x > y && ( x >=0 || y >= 0)) || (x < y && (x<0 || y < 0))) {
                                //Validating if the displacement is +ve or -ve.
                                System.out.println("In if");
                                if(x >= 200) {
                                    gestureDetected = true;
                                    if(isPPT){
                                        robot.keyPress(KeyEvent.VK_RIGHT);
                                    }else{
                                        robot.keyPress(KeyEvent.VK_CONTROL);
                                        robot.keyPress(KeyEvent.VK_F);
                                        robot.keyRelease(KeyEvent.VK_CONTROL);
                                    }
                                }
                                else if( x <= -200) {
                                    gestureDetected = true;
                                    if(isPPT){
                                        robot.keyPress(KeyEvent.VK_LEFT);
                                    }else{
                                        robot.keyPress(KeyEvent.VK_CONTROL);
                                        robot.keyPress(KeyEvent.VK_B);
                                        robot.keyRelease(KeyEvent.VK_CONTROL);
                                    }
                                }
                            }else{
                                System.out.println("In else");
                                if(y >= 200) {
                                    System.out.println("In else UP");
                                    gestureDetected = true;
                                    if(isPPT){
                                        robot.keyPress(KeyEvent.VK_F5);
                                    }
                                    else{
                                        robot.keyPress(KeyEvent.VK_CONTROL);
                                        robot.keyPress(KeyEvent.VK_P);
                                        robot.keyRelease(KeyEvent.VK_CONTROL);
                                    }
                                }
                                else if(y <= -200) {
                                    System.out.println("In else DOWN");
                                    gestureDetected = true;
                                    if(isPPT){
                                        robot.keyPress(KeyEvent.VK_ESCAPE);
                                    }
                                    else{
                                        robot.keyPress(KeyEvent.VK_CONTROL);
                                        robot.keyPress(KeyEvent.VK_P);
                                        robot.keyRelease(KeyEvent.VK_CONTROL);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        this.isAnchorPoint = false;
                        this.gestureDetected = false;
                    }
                    this.mat = temp;
                    this.jlDisplay.setIcon(new ImageIcon(ImageProcessor.toBufferedImage(temp)));
                }  
                else{  
                    System.out.println(" -- Frame not captured -- Break!"); 
                    break;  
                }
            }  
        }
        else{
                System.out.println("Couldn't open capture.");
        }
    }
    /**
    * This function traverses the given matrix to check the white pixels and returns the X-co-ordinate of 
    * of those pixels.
    * @param : source : The matrix in which pointer needs to extracted.
    * @returns : X-Coordinate of the pixel if found otherwise it returns -1;
    **/
    private int getX(Mat source) {
        byte[] buffer =  new byte[(int)(source.total() * source.elemSize())];
        source.get(0, 0, buffer);
        for(int i=0;i<source.height();i++) {
            for(int j=0;j<source.width();j++) {
                if(buffer[(source.width()*i) + j] == -1) {
                    return j;
                }
            }
        }
      
        return -1;
    }
    /**
    * This function traverses the given matrix to check the white pixels and returns the Y-co-ordinate of 
    * of those pixels.
    * @param : source : The matrix in which pointer needs to extracted.
    * @returns : Y Co ordinate of the pixel if found otherwise it returns -1;
    **/
    private int getY(Mat source) {
        byte[] buffer =  new byte[(int)(source.total() * source.elemSize())];
        source.get(0, 0, buffer);
        for(int i=0;i<source.height();i++) {
            for(int j=0;j<source.width();j++) {
                if(buffer[(source.width()*i) + j] == -1) {
                    return i;
                }
            }
        }
        return -1;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsp = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jlDisplay = new javax.swing.JLabel();
        jRBtnPPT = new javax.swing.JRadioButton();
        jRBtnMediaPlayer = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jsp.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.add(jlDisplay);

        jsp.setViewportView(jPanel1);

        jRBtnPPT.setForeground(new java.awt.Color(89, 184, 137));
        jRBtnPPT.setText("Powerpoint Presentation");
        jRBtnPPT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnPPTActionPerformed(evt);
            }
        });

        jRBtnMediaPlayer.setForeground(new java.awt.Color(89, 184, 137));
        jRBtnMediaPlayer.setText("Window's Media Player");
        jRBtnMediaPlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBtnMediaPlayerActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Poppins Medium", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(89, 184, 137));
        jLabel2.setText("Select The Application To Be Controlled By Laser");

        jLabel1.setFont(new java.awt.Font("Poppins SemiBold", 1, 20)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(89, 184, 137));
        jLabel1.setText("LASER GESTURE RECOGNITION");

        jLabel3.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(89, 184, 137));
        jLabel3.setText("1. Left");

        jLabel4.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(89, 184, 137));
        jLabel4.setText("Gestures Recognized are:");

        jLabel5.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(89, 184, 137));
        jLabel5.setText("2. Right");

        jLabel6.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(89, 184, 137));
        jLabel6.setText("3. Up");

        jLabel7.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(89, 184, 137));
        jLabel7.setText("4. Down");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jsp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRBtnPPT)
                        .addGap(53, 53, 53)
                        .addComponent(jRBtnMediaPlayer))
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(121, 121, 121))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(103, 103, 103))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(340, 340, 340))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRBtnMediaPlayer)
                            .addComponent(jRBtnPPT)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsp, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jRBtnPPTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnPPTActionPerformed
        // TODO add your handling code here:
        isPPT = true;
    }//GEN-LAST:event_jRBtnPPTActionPerformed

    private void jRBtnMediaPlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBtnMediaPlayerActionPerformed
        // TODO add your handling code here:
        isPPT = false;
    }//GEN-LAST:event_jRBtnMediaPlayerActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LaserTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LaserTest().setVisible(true);
            }
        });
    }
    private Mat mat;
    private boolean isAnchorPoint = false;
    private boolean gestureDetected = false;
    private int anchorX,anchorY;
    private Robot robot;
    private boolean isPPT = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRBtnMediaPlayer;
    private javax.swing.JRadioButton jRBtnPPT;
    private javax.swing.JLabel jlDisplay;
    private javax.swing.JScrollPane jsp;
    // End of variables declaration//GEN-END:variables
}
