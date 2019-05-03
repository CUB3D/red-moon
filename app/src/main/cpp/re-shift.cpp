/*
 * Copyright (c) 2017 Joona <joonatoona@digitalfishfun.com>
 * SPDX-License-Identifier: GPL-3.0+
 */

#include <fstream>
#include <string>
#include <iostream>
#include <sstream>

const static std::string STOP_COMMAND = "service call SurfaceFlinger 1015 i32 0";

void runCommand(const std::string& cmd) {
    popen(cmd.c_str(), "r");
}

void applyMatrix(const float r, const float g, const float b) {
    //Don't allow for all 0's to be sent
    if(r == 0 && g == 0 && b == 0) {
        return;
    }

    std::stringstream ss;
    ss << "service call SurfaceFlinger 1015 i32 1 ";

    float matrix[16] = {
            r, 0, 0, 0,
            0, g, 0, 0,
            0, 0, b, 0,
            0, 0, 0, 1
    };

    for(float& f : matrix) {
        ss << "f ";
        ss << f;
        ss << " ";
    }

    std::cout << ss.str();

    runCommand(ss.str());
}

void fifoPoll(const std::string& fifoPath) {
    float r,g,b, oldR,oldG,oldB;

    r = g = b = oldR = oldG = oldB = 1;

    while(true) {
        std::ifstream f_pipe(fifoPath);
        std::string line;
        std::cout << "Created pipe\n";

        while(std::getline(f_pipe, line)) {
            std::cout << "Got command " << line << "\n";

            if (line == "exit") {
                std::cout << "Received exit!\n";
                return;
            }

            std::stringstream ss;
            ss << line;
            ss >> r;
            ss >> g;
            ss >> b;

            if(r != oldR || g != oldG || b != oldB) {
                std::cout << "R: " << r << ", G: " << g << ", B: " << b << "\n";

                oldR = r;
                oldG = g;
                oldB = b;

                applyMatrix(r, g, b);
            }
        }

        f_pipe.close();
    }
}

int main(int argc, char** argv) {
	if (argc < 2) {
		std::cout << "Usage: " << argv[0] << " <path/to/fifo>\n";
		return 1;
	}

	std::cout << "Waiting for input from '" << argv[1] << "'\n";

	fifoPoll(argv[1]);

    runCommand(STOP_COMMAND);

    std::cout << "Goodbye!\n";

    return 0;
}
