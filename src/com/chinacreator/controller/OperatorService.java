package com.chinacreator.controller;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import javax.imageio.ImageIO;

import com.chinacreator.common.Global;
import com.chinacreator.service.DataOprService;

public class OperatorService {
	//获取配置文件
	private static Map<String,String> conf=DataOprService.getInstance().getProp();
    private ObjectInputStream dis = null;
	
	private OperatorService(){
		
	}
	private static OperatorService operatorService;
	
	public static OperatorService getInstance(){
		return operatorService==null?new OperatorService():operatorService;
	}
	/**
	 * @Description
	 * 摆渡客户端操作指令
	 * 客户端socket信息流中第1个字节是操作指令
	 * @Author qiang.zhu
	 * @param socket
	 * @return
	 */
    public void func(Socket socket){
        //重新获取配置文件，保持修改路径时实时生效
        conf=DataOprService.getInstance().getProp();
        try {
            dis = new ObjectInputStream(socket.getInputStream());
            DataOprService.getInstance().insertLog("请求客户端:"+socket.getRemoteSocketAddress().toString(),conf.get(Global.MAIN_PATH));
            int flag=dis.readInt();
            //客户端发起传送文件
            if(flag==1){
            	sendImage(socket);
            }
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("func:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
    }

	/**
	 * @Description
	 * 摆渡客户端操作指令
	 * 客户端socket信息流中第1个字节是操作指令
	 * @Author qiang.zhu
	 * @param socket
	 * @return
	 */
	private void sendImage(Socket socket){
        try {
    		ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream());
            try {
            	new Thread(new Runnable() {
					
					@Override
					public void run() {
		            	try {
		            		Robot rob=new Robot();
		            		while(true){
		            			InputEvent obj=(InputEvent)dis.readObject();
								actionEvent(rob,obj);
		            		}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
            	while(true){
	                byte[] data=getImage();
	                dos.writeInt(data.length);
	                dos.write(data);
	                dos.flush();
	            	Thread.sleep(Integer.parseInt(conf.get("times")));
            	}
            } finally {
                if (dis != null)
                	dis.close();
                if (dos != null)
                    dos.close();
                if (socket != null)
                	socket.close();
            }
        }catch (Exception e) {
        	DataOprService.getInstance().insertLog("sendImage:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
	}
	/**
	 * @Description
	 * 摆渡客户端操作指令
	 * 客户端socket信息流中第1个字节是操作指令
	 * @Author qiang.zhu
	 * @param socket
	 * @return
	 */
	private byte[] getImage(){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
			Toolkit tk=Toolkit.getDefaultToolkit();
			Robot rb=new Robot();
			Dimension dm=tk.getScreenSize();
			Rectangle rt=new Rectangle(dm);
			BufferedImage bi=rb.createScreenCapture(rt);
			ImageIO.write(bi, "jpeg", baos);
		}catch(Exception e){
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	private void actionEvent(Robot robot,InputEvent e) throws Exception {
        if (e instanceof java.awt.event.KeyEvent) {
            KeyEvent ke = (KeyEvent) e;
            int type = ke.getID();
            if (type == java.awt.Event.KEY_PRESS) {
                robot.keyPress(ke.getKeyCode());
            }
            if (type == java.awt.Event.KEY_RELEASE) {
                robot.keyRelease(ke.getKeyCode());
            }
 
        }
        if (e instanceof java.awt.event.MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            int type = e.getID();
            if (type == java.awt.Event.MOUSE_DOWN) {
                robot.mousePress(getMouseClick(me.getButton()));
            }else if (type == java.awt.Event.MOUSE_UP) {
                robot.mouseRelease(getMouseClick(me.getButton()));
            }else if (type == java.awt.Event.MOUSE_MOVE) {
                robot.mouseMove(me.getX(), me.getY());
            } else if(type == Event.MOUSE_DRAG) {
                robot.mouseMove(me.getX(), me.getY());
            }
 
        }
 
    }
	/**
     * 根据发送事的Mouse事件对象，转变为通用的Mouse按键代码
     * @param button
     * @return
     */
    private int getMouseClick(int button) {
        if (button == MouseEvent.BUTTON1) {
            return InputEvent.BUTTON1_MASK;
        }
        if (button == MouseEvent.BUTTON2) {
            return InputEvent.BUTTON2_MASK;
        }
        if (button == MouseEvent.BUTTON3) {
            return InputEvent.BUTTON3_MASK;
        }
        return -1;
    }
}
