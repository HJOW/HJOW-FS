/*
Copyright 2024 HJOW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.hjow.fs.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hjow.fs.FSProtocolHandler;

public class FSServlet extends HttpServlet {
	private static final long serialVersionUID = -8577798339129756545L;
	
	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init(cfg);
	}
	
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException {
		System.out.println("Servlet called");
		FSProtocolHandler.getInstnace().handle(req, res);
	}
}
