#define _XOPEN_SOURCE /* glibc2 needs this */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <math.h>

struct plant {
	/* input */
	char		*name;
	unsigned int	num_plants_to_harvest;
	unsigned int	num_weeks_indoors;
	unsigned int	num_weeks_until_indoor_separation;
	struct tm	outdoor_planting_date;
	unsigned int	num_weeks_until_outdoor_separation;
	unsigned int	days_to_harvest;
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
	return 1;
}

int copy_word_from_file(FILE *fp, char **new_word)
{
	char word[MAX_NAME_LENGTH];

	fscanf(fp, "%[^,]", word);
	*new_word = malloc(strlen(word) + 1);
	if (!*new_word)
		return 0;
	strcpy(*new_word, word);
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
	memset(new_plant, 0, sizeof(*new_plant));

	if(!skip_comment_lines(fp))
		return NULL;

	/* Get the plant name */
	if (!copy_word_from_file(fp, &new_plant->name))
		return NULL;

	/* Get the number plants we want to harvest */
	fscanf(fp, "%u", &new_plant->num_plants_to_harvest);
	fgetc(fp);

	/* Get the number of weeks indoors */
	fscanf(fp, "%u", &new_plant->num_weeks_indoors);
	fgetc(fp);

	/* Get the number of weeks after sprouting
	 * that we need to separate the plants.
	 */
	fscanf(fp, "%u", &new_plant->num_weeks_until_indoor_separation);
	fgetc(fp);

	/* Convert the outdoor planting date into something we can understand */
	if (!copy_word_from_file(fp, &string))
		return NULL;
	if (!strptime(string, "%Y-%m-%d", &new_plant->outdoor_planting_date))
		return NULL;
	free(string);
	
	fscanf(fp, "%u", &new_plant->num_weeks_until_outdoor_separation);
	fgetc(fp);

	fscanf(fp, "%u", &new_plant->days_to_harvest);
	fgetc(fp);

	fscanf(fp, "%f", &new_plant->germination_rate);
	fgetc(fp);

	fscanf(fp, "%u", &new_plant->avg_days_to_sprout);
	fgetc(fp);

	fscanf(fp, "%u", &new_plant->harvest_removes_plant);
	fgetc(fp);

	return new_plant;
}

int add_days_to_date(struct tm *date, int days)
{
	date->tm_mday += days;
	/* Normalize the date */
	mktime(date);
	return 0;
}

int calculate_plant_dates(struct plant *new_plant)
{
	int ret;

	/* Some plants need to be direct sown outdoors,
	 * rather than started under a sun lamp indoors.
	 */
	if (new_plant->num_weeks_indoors) {
		new_plant->seeding_date = new_plant->outdoor_planting_date;
		ret = add_days_to_date(&new_plant->seeding_date,
				-(new_plant->num_weeks_indoors)*7);
		if (ret)
			return ret;

		new_plant->sprouting_date = new_plant->seeding_date;
	} else {
		new_plant->sprouting_date = new_plant->outdoor_planting_date;
	}
	ret = add_days_to_date(&new_plant->sprouting_date,
			new_plant->avg_days_to_sprout);
	if (ret)
		return ret;

	return 0;
}

void print_sprouting_date(struct plant *new_plant)
{
	char string[MAX_NAME_LENGTH];

	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->sprouting_date);
	printf("Expect sprouting seeds around: %s\n", string);
}

void print_action_dates(struct plant *new_plant)
{
	char string[MAX_NAME_LENGTH];
	float num_seeds;
	char *planting_type;
	int chars_printed;

	chars_printed = printf("Calendar for %s:\n", new_plant->name);
	/* Don't count the newline */
	for(; chars_printed > 1; chars_printed--)
		putchar('=');
	printf("\n");

	if (!new_plant->num_weeks_indoors)
	{
		planting_type = "Direct sow";
	} else {
		strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
				&new_plant->seeding_date);
		/*
		 * num seeds survived = num seeds planted * germination rate
		 * num seeds survived / germination rate = num seeds planted
		 */
		num_seeds = ceil(new_plant->num_plants_to_harvest / new_plant->germination_rate);
		printf("Start %i seed%s under grow lamp: %s\n",
				(int) num_seeds,
				(num_seeds > 1) ? "s" : "",
				string);
		print_sprouting_date(new_plant);
		planting_type = "Transplant";
	}

	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->outdoor_planting_date);
	printf("%s outdoors: %s\n", planting_type, string);
	if (!new_plant->num_weeks_indoors)
		print_sprouting_date(new_plant);
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
	while (1) {
		new_plant = parse_and_create_plant(fp);
		if (!new_plant)
			return -1;
		calculate_plant_dates(new_plant);
		printf("\n");
		print_action_dates(new_plant);
		printf("\n");
	}

	return 0;
}
