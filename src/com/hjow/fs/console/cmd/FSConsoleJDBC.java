/*
Copyright 2019 HJOW

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
package com.hjow.fs.console.cmd;

import java.io.File;
import java.util.Map;

import com.hjow.fs.FSControl;
import com.hjow.fs.console.FSConsole;

import hjow.common.script.jdbc.JDBCConnection;
import hjow.common.util.ClassUtil;
import hjow.common.util.DataUtil;
import hjow.common.util.JDBCUtil;

public class FSConsoleJDBC implements FSBundledConsoleCommand {
	private static final long serialVersionUID = -4460440826046466395L;

	@Override
    public String getName() {
        return "jdbc";
    }

    @Override
    public String getShortName() {
        return "jdbc";
    }

    @Override
    public Object run(FSControl ctrl, FSConsole console, Map<String, Object> sessionMap, File root, String parameter, Map<String, String> options) throws Throwable {
    	if(ctrl.isReadOnly()) throw new RuntimeException("Blocked. FS is read-only mode.");
    	
    	String idtype = sessionMap.get("idtype").toString().toUpperCase().trim();
        if(! idtype.equals("A")) throw new RuntimeException("No privileges");
        
        String jdbcClass, jdbcUrl, jdbcId, jdbcPw, sql;
        jdbcClass = null;
        jdbcUrl   = null;
        jdbcId    = null;
        jdbcPw    = null;
        sql       = parameter.trim();
        
        if(ctrl.isUseJDBC()) {
        	jdbcClass = ctrl.getJdbcClass();
        	jdbcUrl   = ctrl.getJdbcUrl();
        	jdbcId    = ctrl.getJdbcId();
        	jdbcPw    = ctrl.getJdbcPw();
        }
        
        if(DataUtil.isNotEmpty(options.get("driver"   ))) jdbcClass = options.get("driver");
        if(DataUtil.isNotEmpty(options.get("class"    ))) jdbcClass = options.get("class");
        if(DataUtil.isNotEmpty(options.get("url"      ))) jdbcUrl   = options.get("url");
        if(DataUtil.isNotEmpty(options.get("id"       ))) jdbcId    = options.get("id");
        if(DataUtil.isNotEmpty(options.get("username" ))) jdbcId    = options.get("username");
        if(DataUtil.isNotEmpty(options.get("pw"       ))) jdbcPw    = options.get("pw");
        if(DataUtil.isNotEmpty(options.get("password" ))) jdbcPw    = options.get("password");
        
        if(DataUtil.isEmpty(jdbcClass)) throw new RuntimeException("JDBC driver not specified.");
        if(DataUtil.isEmpty(jdbcUrl  )) throw new RuntimeException("JDBC URL not specified.");
        if(DataUtil.isEmpty(jdbcId   )) throw new RuntimeException("JDBC username not specified.");
        if(DataUtil.isEmpty(jdbcPw   )) throw new RuntimeException("JDBC password not specified.");
        
        JDBCConnection conn = null;
        Throwable caught = null;
        Object res = null;
        try {
        	conn = JDBCUtil.connect(jdbcClass, jdbcUrl, jdbcId, jdbcPw);
        	boolean isSelect = (sql.toLowerCase().startsWith("select"));
        	
        	if(isSelect) {
        		res = conn.select(sql, null);
        	} else {
        		res = conn.update(sql, null);
        	}
        	try { conn.commit(); } catch(Throwable ignores) {}
        } catch(Throwable tx) {
        	caught = tx;
        	try { conn.rollback(); } catch(Throwable ignores) {}
        } finally {
        	ClassUtil.closeAll(conn);
        }
        
        if(caught != null) throw caught;
        return res;
    }

    @Override
    public String getHelp(String lang, boolean detail) {
        StringBuilder res = new StringBuilder("");
        if(detail) {
            if(lang.equals("ko")) {
                res = res.append(" * jdbc").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    JDBC 에 액세스하여 SQL문을 실행합니다.                              ").append("\n");
                res = res.append("    관리자 권한이 필요합니다.                                           ").append("\n");
                res = res.append("    매개변수로 SQL문을 받습니다.                                        ").append("\n");
                res = res.append("    트랜잭션 사용이 불가능하며, 정상 실행 후 바로 커밋됩니다.           ").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * 예").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    jdbc 'SELECT * FROM DUAL'                                           ").append("\n");
                res = res.append("                                                                        ").append("\n");
            } else {
                res = res.append(" * jdbc").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    Access JDBC and run SQL statements.                                 ").append("\n");
                res = res.append("    Need admin privileges.                                              ").append("\n");
                res = res.append("    Need parameter as a SQL statement.                                  ").append("\n");
                res = res.append("    No transaction. If the statement run complete, then it will be committed automatically.").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append(" * example").append("\n");
                res = res.append("                                                                        ").append("\n");
                res = res.append("    jdbc 'SELECT * FROM DUAL'                                           ").append("\n");
                res = res.append("                                                                        ").append("\n");
            }
        } else {
            if(lang.equals("ko")) {
                res = res.append("JDBC에 액세스하여 SQL문을 실행합니다.").append("\n");
            } else {
                res = res.append("Access JDBC and run SQL statements.").append("\n");
            }
        }
        return res.toString().trim();
    }
}
