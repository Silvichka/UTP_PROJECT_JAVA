cmake_minimum_required(VERSION 3.29)
project(Project)

set(CMAKE_CXX_STANDARD 20)

add_library(Project SHARED
        GUI_Board_CheckListener.cpp)

target_include_directories(Project PRIVATE "/Users/silvia/Library/Java/JavaVirtualMachines/openjdk-23/Contents/Home/include")
target_include_directories(Project PRIVATE "/Users/silvia/Library/Java/JavaVirtualMachines/openjdk-23/Contents/Home/include/darwin")
