package org.application.apihandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableAutoConfiguration
@EnableWebMvc
@Configuration
@ComponentScan
public class ApiHandlerApplication {

	// define default output
	// System.getProperty("user.home") + 
	private static final String DEFAULT_OUTPUT = "/data";//String.format("/Users/%s/Documents/DeFi-Data", System.getProperty("user.name"));
	
	private static final HashMap<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
	private static final HashMap<String, ArrayList<HashMap<String, String>>> buffers = new HashMap<String, ArrayList<HashMap<String, String>>>();
	private static final HashMap<String, TreeSet<String>> headers = new HashMap<String, TreeSet<String>>();
	
	private static final void initialize() {
		// retrieve home directory and path to default output
		File dir = new File(DEFAULT_OUTPUT);
		// validate that it is a directory and if not then create
		if(!dir.exists() || !dir.isDirectory())
			dir.mkdir();
	}
	
	public final static void lock(String name) {
		if(!writers.containsKey(name)) {
			try {
				String file_name = DEFAULT_OUTPUT + "/" + name + ".csv";
				File file = new File(file_name);
				if(!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				if(!file.exists())
					file.createNewFile();
				else {
					file.delete();
					file.createNewFile();
				}
				writers.put(name, new BufferedWriter(new FileWriter(file)));
				buffers.put(name, new ArrayList<HashMap<String, String>>());
				headers.put(name, new TreeSet<String>());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public final static void unlock(String name) {
		if(writers.containsKey(name)) {
			try {
				// retrieve writer
				BufferedWriter writer = writers.get(name);
				
				// write all column headers
				Iterator<String> header_itr = headers.get(name).iterator();
				while(header_itr.hasNext()) {
					writer.write(header_itr.next());
					if(header_itr.hasNext())
						writer.write(",");
				}
				
				// new line
				writer.write("\n");
				
				// write to file using formatted headers
				ArrayList<HashMap<String, String>> buffer = buffers.get(name);
				for(int i = 0; i < buffer.size(); i++) {
					StringBuilder line = new StringBuilder();
					
					// loop through all headers and format
					for(String header : headers.get(name)) {
						if(buffer.get(i).containsKey(header)) {
							line.append(buffer.get(i).get(header).replaceAll(",", "|"));
						}
						
						line.append(",");
					}
					
					// write line to file
					line.delete(line.length() - 1, line.length());
					writer.write(line.toString());
					
					// new line
					if(i != buffer.size() - 1)
						writer.write("\n");
				}
				
				// close output loop
				writers.get(name).close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		writers.remove(name);
	}
	
	public final static void output(String name, HashMap<String, String> data) {
		if(!writers.containsKey(name) || !buffers.containsKey(name) || !headers.containsKey(name)) {
			lock(name);
		}
		
		// add all headers
		for(String header : data.keySet())
			headers.get(name).add(header);
		
		// push data to buffers
		buffers.get(name).add(data);
	}
	
	public static void main(String[] args) {
		// load in output directory
		initialize();
		
		SpringApplication.run(ApiHandlerApplication.class, args);
	}
}