![](https://img.shields.io/badge/lohigh_1.0-passing-light_green)
![](https://img.shields.io/badge/lohigh_2.0-passing-green)
![](https://img.shields.io/badge/lohigh_3.0-java-orange)

# `lohigh`

DJ Sacabambaspis lets you take lofi on the go.

![](asset/fish.jpg)

## installation

```console
$ git clone https://github.com/gongahkia/lohigh
$ cd lohigh
$ make config
```

## usage

```console
$ make build
$ java -cp src Main input.wav output.wav # DJ Sacabambaspis mixes up a lofi beat
$ java -cp src Main input1.wav input2.wav output.wav # You are the DJ
```

## requirements

- Java 8 or higher
- No external dependencies (uses standard `javax.sound.sampled` library)
