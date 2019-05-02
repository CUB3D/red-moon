/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

#include <fstream>
#include <string>
#include <iostream>


int main(int argc, char** argv) {
	if (argc < 2)
	{
		std::cout << "Usage: " << argv[0] << " <path/to/fifo>\n";
		return 1;
	}

	std::cout << "Waiting for input from '" << argv[1] << "'\n";

	std::cout << "connecting to fifo\n";
	FILE* file = fopen("/data/local/tmp/fifo", "r");
	std::cout << "fifo open";
	
	std::ifstream f_pipe("/data/local/tmp/fifo");//(argv[1]);
	std::cout << "Created pipe\n";
	for (std::string line; std::getline(f_pipe, line);)
	{
	    std::cout << "Got command " << line << "\n";

		if (line == "exit")
		{
			std::cout << "Received exit!\n";
			break;
		}
		
		std::string cmd("service call SurfaceFlinger ");
		cmd.append(line);
		popen(cmd.c_str(), "r");
	}
	
	std::cout << "Goodbye!\n";
	return 0;
}
