package com.hjow.fs.console.cmd;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.hjow.fs.console.FSConsole;

public class FSConsoleHelp implements FSConsoleCommand {
	private static final long serialVersionUID = -1045910686449876799L;

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getShortName() {
		return "h";
	}

	@Override
	public Object run(FSConsole console, Map<String, Object> sessionMap, File root, String parameter) throws Throwable {
		StringBuilder res = new StringBuilder("");
		
		String lang = String.valueOf(sessionMap.get("lang"));
		
		if(parameter == null) parameter = "";
		parameter = parameter.trim();
		if(parameter.equals("")) {
			if(lang.equals("ko")) {
				res = res.append(" * FS콘솔 기본 사용법").append("\n");
				res = res.append("                                                                      ").append("\n");
				res = res.append("    명령어를 쓰고 한 칸을 띄운 다음, 매개 변수(입력 데이터)를 입력하고").append("\n");
				res = res.append("    엔터 키 또는 페이지 내 '>' 버튼을 누릅니다.                       ").append("\n");
				res = res.append("    매개 변수가 필요 없는 명령어도 있습니다.                          ").append("\n");
				res = res.append("                                                                      ").append("\n");
				res = res.append(" * 명령어 목록").append("\n");
				res = res.append("                                                                      ").append("\n");
				List<FSConsoleCommand> commands = console.getCommands();
				for(FSConsoleCommand c : commands) {
					res = res.append(c.getName()).append("\t: ").append(c.getHelp(lang, false)).append("\n");
				}
			} else {
				res = res.append(" * Basic Usage of FS Console").append("\n");
				res = res.append("                                                                      ").append("\n");
				res = res.append("    Write a command word, one space, then write parameters.           ").append("\n");
				res = res.append("    Then, press enter key, or '>' button on the page.                 ").append("\n");
				res = res.append("    Parameters may not be necessary on some command words.            ").append("\n");
				res = res.append("                                                                      ").append("\n");
				res = res.append(" * Command word list").append("\n");
				res = res.append("                                                                      ").append("\n");
				List<FSConsoleCommand> commands = console.getCommands();
				for(FSConsoleCommand c : commands) {
					res = res.append(c.getName()).append("\t: ").append(c.getHelp(lang, false)).append("\n");
				}
			}
		} else {
			if(lang.equals("ko")) {
				FSConsoleCommand cm = null;
				List<FSConsoleCommand> commands = console.getCommands();
				for(FSConsoleCommand c : commands) {
					if(parameter.equals(c.getName()) || parameter.equals(c.getShortName())) { cm = c; break; }
				}
				if(cm == null) throw new RuntimeException("대상 명령어를 찾을 수 없습니다. " + parameter);
				res = res.append(cm.getHelp(lang, true));
			} else {
				FSConsoleCommand cm = null;
				List<FSConsoleCommand> commands = console.getCommands();
				for(FSConsoleCommand c : commands) {
					if(parameter.equals(c.getName()) || parameter.equals(c.getShortName())) { cm = c; break; }
				}
				if(cm == null) throw new RuntimeException("No such that command word. " + parameter);
				res = res.append(cm.getHelp(lang, true));
			}
		}
		
		return res.toString();
	}

	@Override
	public String getHelp(String lang, boolean detail) {
		StringBuilder res = new StringBuilder("");
		if(detail) {
			if(lang.equals("ko")) {
				res = res.append(" * help").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    실행 시 도움말을 출력합니다.").append("\n");
				res = res.append("    매개변수로 다른 명령어를 넣으면 그 명령어의 도움말을 볼 수 있습니다.").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * 예").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    help cat                                                            ").append("\n");
				res = res.append("                                                                        ").append("\n");
			} else {
				res = res.append(" * help").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    Show you a help message.                                            ").append("\n");
				res = res.append("    If you input other command as a parameter,                          ").append("\n");
				res = res.append("    then you can see help messages of that command.                     ").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append(" * example").append("\n");
				res = res.append("                                                                        ").append("\n");
				res = res.append("    help cat                                                            ").append("\n");
				res = res.append("                                                                        ").append("\n");
			}
		} else {
			if(lang.equals("ko")) {
				res = res.append("이 도움말을 보는 데 사용되는 명령어입니다. 매개변수로 다른 명령어를 넣으면 그 명령어의 도움말을 볼 수 있습니다.").append("\n");
			} else {
				res = res.append("The command to see this help message. If you input other command as a parameter, then you can see help messages of that command.").append("\n");
			}
		}
		return res.toString().trim();
	}
}
