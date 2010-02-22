#define _XOPEN_SOURCE /* glibc2 needs this */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>

struct plant {
	/* input */
	char		*name;
	unsigned int	num_plants_to_harvest;
	unsigned int	num_weeks_indoors;
	unsigned int	num_weeks_until_indoor_separation;
	struct tm	outdoor_planting_date;
	unsigned int	num_weeks_until_outdoor_separation;
	float		germination_rate;
	unsigned int	avg_days_to_sprout;
	unsigned int	harvest_removes_plant; /* 0 = false; non-zero = true */
	/* output */
	/* XXX: These could be an array with an enum. */
	struct tm	seeding_date;
	struct tm	sprouting_date;
	struct tm	separation_date; /* Could be for separating plants into new pots
				  * after seeding indoors, or thinning plants
				  * after direct sowing.
				  */
	struct tm	harden_off_date;
	struct tm	harvest_date;
};

#define MAX_NAME_LENGTH	500

int skip_comment_lines(FILE *fp)
{
	int tmp, tmp2;
	char string[MAX_NAME_LENGTH];

	/* Skip lines that start with # */
	do {
		tmp = fgetc(fp);
		if (tmp == EOF)
			return 0;

		string[0] = (char) tmp;
		/* Put the last character peeked at back */
		tmp2 = ungetc(tmp, fp);
		if (tmp2 == EOF)
			return 0;

		if (string[0] != '#')
			break;
		/* Eat up to the newline */
		tmp2 = fscanf(fp, "%[^\n]", string);
		if (tmp2 == EOF)
			return 0;
		/* Eat the newline */
		tmp = fgetc(fp);
	} while (1);
	printf("# finished do-while loop\n");
	return 1;
}

int copy_word_from_file(FILE *fp, char **new_word, const char *description)
{
	char word[MAX_NAME_LENGTH];

	fscanf(fp, "%[^,]", word);
	*new_word = malloc(strlen(word) + 1);
	if (!*new_word)
		return 0;
	strcpy(*new_word, word);
	printf("%s: %s\n", description, *new_word);
	fgetc(fp);
	return 1;
}

struct plant *parse_and_create_plant(FILE *fp)
{
	struct plant *new_plant;
	char *string;

	new_plant = malloc(sizeof(*new_plant));
	if (!new_plant) {
		printf("Out of memory\n");
		return NULL;
	}

	if(!skip_comment_lines(fp))
		return NULL;

	/* Get the plant name */
	if (!copy_word_from_file(fp, &new_plant->name, "Plant name"))
		return NULL;

	/* Get the number plants we want to harvest */
	fscanf(fp, "%u", &new_plant->num_plants_to_harvest);
	fgetc(fp);
	printf("Number of plants to harvest: %u\n", new_plant->num_plants_to_harvest);

	/* Get the number of weeks indoors */
	fscanf(fp, "%u", &new_plant->num_weeks_indoors);
	fgetc(fp);
	printf("Number of weeks indoors: %u\n", new_plant->num_weeks_indoors);

	/* Get the number of weeks after sprouting
	 * that we need to separate the plants.
	 */
	fscanf(fp, "%u", &new_plant->num_weeks_until_indoor_separation);
	fgetc(fp);
	printf("Number of weeks indoors until separation: %u\n",
			new_plant->num_weeks_until_indoor_separation);

	/* Convert the outdoor planting date into something we can understand */
	if (!copy_word_from_file(fp, &string, "Outdoor planting date"))
		return NULL;
	if (!strptime(string, "%Y-%m-%d", &new_plant->outdoor_planting_date))
		return NULL;
	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->outdoor_planting_date);
	printf("Parsed outdoor planting date: %s\n", string);
	return new_plant;
}

int main (int argc, char *argv[])
{
	FILE *fp;
	struct plant *new_plant;

	if (argc < 2) {
		printf("Help: plant <file>.\n");
		return -1;
	}
	fp = fopen(argv[1], "r");
	if (!fp) {
		printf("Bad file.\n");
		return -1;
	}
	new_plant = parse_and_create_plant(fp);
	if (!new_plant)
		return -1;

	return 0;
}
