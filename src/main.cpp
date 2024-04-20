#include <iostream>
#include <vector>
#include <cstring>
#include <sndfile.h>

// ----- helper function definition -----
    // there's also like just one function
    // nice

bool combineSoundFiles(const char* inputFile1, const char* inputFile2, const char* outputFile) {

    SF_INFO inputInfo1, inputInfo2, outputInfo;
    SNDFILE *inputSndFile1, *inputSndFile2, *outputSndFile;
    sf_count_t count;
    std::vector<short> buf1, buf2;

    // read first file
    inputSndFile1 = sf_open(inputFile1, SFM_READ, &inputInfo1);
    if (!inputSndFile1) {
        std::cerr << "DJ Sacabambaspis can't open the input file named '" << inputFile1 << "': " << sf_strerror(nullptr) << std::endl;
        return false;
    }

    // read second file
    inputSndFile2 = sf_open(inputFile2, SFM_READ, &inputInfo2);
    if (!inputSndFile2) {
        std::cerr << "DJ Sacabambaspis can't open the input file named '" << inputFile2 << "': " << sf_strerror(nullptr) << std::endl;
        sf_close(inputSndFile1);
        return false;
    }

    // retrieve first file properties
    outputInfo.samplerate = inputInfo1.samplerate;
    outputInfo.channels = inputInfo1.channels;
    outputInfo.format = inputInfo1.format;

    // write to output sound file
    outputSndFile = sf_open(outputFile, SFM_WRITE, &outputInfo);
    if (!outputSndFile) {
        std::cerr << "DJ Sacabambaspis can't open the output file named '" << outputFile << "': " << sf_strerror(nullptr) << std::endl;
        sf_close(inputSndFile1);
        sf_close(inputSndFile2);
        return false;
    }

    // check buffer sizes based on frames count of input files
    buf1.resize(inputInfo1.frames * inputInfo1.channels);
    buf2.resize(inputInfo2.frames * inputInfo2.channels);

    // read data from first input file and write to output file
    count = sf_read_short(inputSndFile1, buf1.data(), buf1.size());
    sf_write_short(outputSndFile, buf1.data(), count);

    // read data from second input file and write to output file
    count = sf_read_short(inputSndFile2, buf2.data(), buf2.size());
    sf_write_short(outputSndFile, buf2.data(), count);

    // handle file closing
    sf_close(inputSndFile1);
    sf_close(inputSndFile2);
    sf_close(outputSndFile);

    std::cout << "DJ Sacabambaspis has successfully made your sound lo-fi." << outputFile << std::endl;
    return true;
}

// ----- actual code execution -----

int main(int argc, char* argv[]) {

    const char* defaultInputFile1 = "asset/ambient.wav"; 
    const char* inputFile1;
    const char* inputFile2;
    const char* outputFile;

    if (argc == 4) { // all arguments provided, user 
        inputFile1 = argv[1];
        inputFile2 = argv[2];
        outputFile = argv[3];
    } else if (argc == 3) { // defaults to specified lofi file
        inputFile1 = defaultInputFile1;
        inputFile2 = argv[1];
        outputFile = argv[2];
    } else { // error reached
        std::cerr << "DJ Sacabambaspis cannot make music because there are an incorrect number of files." << std::endl;
        std::cerr << "Provide either 2 or 3 arguments in the following format: " << argv[0] << " <input_file1.wav> <input_file2.wav> <output_file.wav>" << std::endl;
        std::cerr << "       " << argv[0] << " <input_file2.wav> <output_file.wav>" << std::endl;
        return 1;
    }

    if (combineSoundFiles(inputFile1, inputFile2, outputFile)) {
        return 0;
    } else {
        return 1;
    }
}