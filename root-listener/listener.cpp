#include <fstream>
#include <string>
#include <iostream>


int main(int argc, char** argv) {
	if (argc < 2)
	{
		std::cout << "Usage: " << argv[0] << " <path/to/fifo>\n";
		return 1;
	}
	
	std::fstream f_pipe(argv[1]);
	for (std::string line; std::getline(f_pipe, line);)
	{
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
