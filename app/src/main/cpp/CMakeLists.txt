SET(CMAKE_SYSTEM_NAME Linux)
SET(CMAKE_SYSTEM_VERSION 1)

SET(CMAKE_CXX_COMPILER /usr/bin/arm-linux-gnueabi-g++)

cmake_minimum_required(VERSION 3.7.2)

add_library(re-shift SHARED re-shift.cpp)
