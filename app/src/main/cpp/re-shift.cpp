/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

#include <fstream>
#include <string>
#include <iostream>

const static std::string STOP_COMMAND = "1015 i32 0";


void runCommand(const std::string& cmd) {
	std::string fullCmd = "service call SurfaceFlinger ";
	fullCmd.append(cmd);

	popen(fullCmd.c_str(), "r");
}

int main(int argc, char** argv) {
	if (argc < 2)
	{
		std::cout << "Usage: " << argv[0] << " <path/to/fifo>\n";
		return 1;
	}

	std::cout << "Waiting for input from '" << argv[1] << "'\n";

	while(true) {
        std::ifstream f_pipe(argv[1]);
        std::cout << "Created pipe\n";

        for (std::string line; std::getline(f_pipe, line);) {
            std::cout << "Got command " << line << "\n";

            if (line == "exit") {
                std::cout << "Received exit!\n";
                goto exit;
            }

            runCommand(line);
        }
    }

	exit: {
        runCommand(STOP_COMMAND);

        std::cout << "Goodbye!\n";

        return 0;
    };
}
