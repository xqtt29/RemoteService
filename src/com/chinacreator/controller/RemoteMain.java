package com.chinacreator.controller;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import com.chinacreator.common.Global;
import com.chinacreator.service.DataOprService;

public class RemoteMain {

	private static Map<String,String> conf=DataOprService.getInstance().getProp();
	
	public static void main(String[] args) {
		try {
            final ServerSocket receiveService = new ServerSocket(Integer.parseInt(conf.get(Global.PORT)==null?"7777":conf.get(Global.PORT).toString()));
			while (true) {
				try {
					final Socket socket = receiveService.accept();
					new Thread(new Runnable() {
						public void run() {
							OperatorService.getInstance().func(socket);
						}
					}).start();;
				} catch (Exception e) {
					DataOprService.getInstance().insertLog("main:while:ex:"+e.getStackTrace().toString(),conf.get(Global.MAIN_PATH));
				}
			}
        } catch (Exception e) {
        	DataOprService.getInstance().insertLog("main:ex:"+e.getStackTrace()[0].toString(),conf.get(Global.MAIN_PATH));
        }
	}
}
