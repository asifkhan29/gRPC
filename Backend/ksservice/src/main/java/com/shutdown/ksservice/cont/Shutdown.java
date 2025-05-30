package com.shutdown.ksservice.cont;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Shutdown {
	
	
	 private final ApplicationContext context;

	    public Shutdown(ApplicationContext context) {
	        this.context = context;
	    }

	    @GetMapping("/stop")
	    public void stopApplication() {
	    	System.out.println("workings");
	        Thread shutdownThread = new Thread(() -> {
	            try {
	                Thread.sleep(1000);
	                ((ConfigurableApplicationContext) context).close();
	            } catch (InterruptedException ignored) {}
	        });
	        shutdownThread.setDaemon(false);
	        shutdownThread.start();
	    }

}
