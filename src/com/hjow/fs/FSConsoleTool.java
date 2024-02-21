package com.hjow.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FSConsoleTool implements Serializable {
	private static final long serialVersionUID = -2064065313726221667L;
	private static File rootPath = null;
	private FSConsole console = null;
	
	static void init(File rtPath) {
		if(rootPath != null) return;
		rootPath = rtPath;
	}
	
	FSConsole getConsole() {
		return console;
	}

	void setConsole(FSConsole console) {
		this.console = console;
	}
	
	public Object cd(String dir) throws IOException {
		String pathCalc = rootPath.getCanonicalPath() + File.separator + console.getPath() + File.separator + dir;
		File   fileCalc = new File(pathCalc);
		
		pathCalc = fileCalc.getCanonicalPath();
		
		if(! fileCalc.exists()     ) throw new FileNotFoundException("No such a directory !");
		if(! fileCalc.isDirectory()) throw new FileNotFoundException("No such a directory !");
		if(! pathCalc.startsWith(rootPath.getCanonicalPath())) throw new RuntimeException("Cannot access these path !");
		
		pathCalc = pathCalc.replace(rootPath.getCanonicalPath(), "");
		
		FSConsoleResult rs = new FSConsoleResult();
		rs.setNulll(true);
		rs.setDisplay(null);
		rs.setPath(pathCalc);
		rs.setSuccess(true);
		return rs;
	}
	
	public Object pwd() {
		return console.getPath();
	}
	
	public Object ls() throws IOException {
		String pathCalc = rootPath.getCanonicalPath() + File.separator + console.getPath();
		File   fileCalc = new File(pathCalc);
		
		StringBuilder res = new StringBuilder("");
		res = res.append("-TYPE-\t-SIZE-\t\t-NAME-").append("\n");
		
		List<File> listTwo = new ArrayList<File>();
		File[] listOne = fileCalc.listFiles();
		for(File f : listOne) {
			listTwo.add(f);
		}
		listOne = null;
		
		Collections.sort(listTwo, new Comparator<File>() {
	    	@Override
	    	public int compare(File o1, File o2) {
	    		if(o1.isDirectory() && (! o2.isDirectory())) return -1;
	    		if((! o1.isDirectory()) && o2.isDirectory()) return 1;
	    		return (o1.getName().compareTo(o2.getName()));
	    	}
	    });
		
		for(File f : listTwo) {
			String name = f.getName();
			if(name.startsWith(".")) continue;
			
			if(f.isDirectory()) {
				res = res.append("DIR").append("\t\t\t").append(name).append("").append("\n");
			} else {
				res = res.append("FILE").append("\t").append(FSUtils.getFileSize(f)).append("\t\t").append(name).append("\n");
			}
		}
		
		return res.toString();
	}
	
	
	public Object random() {
		return new Double(Math.random());
	}
	
	public Object abs(Object target) {
		Number n = null;
		if(target instanceof Number) n = (Number) target;
		else                         n = new Double(target.toString());
		return Math.abs(n.doubleValue());
	}
	
	public Object create(Object type) {
		String sType = type.toString().trim();
		
		if(sType.equalsIgnoreCase("list")) return new ArrayList<Object>();
		if(sType.equalsIgnoreCase("map" )) return new HashMap<CharSequence, Object>();
		
		return null;
	}
}
