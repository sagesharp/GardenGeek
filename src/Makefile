pic:
	gcc -Wall -o hello-cairo `pkg-config --cflags --libs cairo` hello-cairo.c && ./hello-cairo && feh hello.png
cal:
	gcc -Wall -g -Wstack-protector -o plant plant.c -lm
clean:
	rm hello-cairo hello.png plant
