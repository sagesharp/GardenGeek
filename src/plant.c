#define _XOPEN_SOURCE 500 /* glibc2 needs this */
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
	struct tm	indoor_separation_date;
	struct tm	hardening_off_date;
	struct tm	outdoor_separation_date;
	struct tm	harvest_date;
};

struct plant_date {
	struct tm	*date;
	char		*blurb;
};

struct date_list {
	struct plant_date	*cal_entry;
	struct date_list	*next;
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

int calculate_indoor_plant_dates(struct plant *new_plant)
{
	int ret;
	unsigned int temp;

	/* Get date to start seeds indoors */
	new_plant->seeding_date = new_plant->outdoor_planting_date;
	ret = add_days_to_date(&new_plant->seeding_date,
			-(new_plant->num_weeks_indoors)*7);
	if (ret)
		return ret;

	/* Get date to separate indoor seedlings */
	if (new_plant->num_weeks_until_indoor_separation) {
		new_plant->indoor_separation_date =
			new_plant->seeding_date;
		temp = new_plant->num_weeks_until_indoor_separation*7;
		ret = add_days_to_date(
				&new_plant->indoor_separation_date,
			       	temp);
		if (ret)
			return ret;
	}

	/* Get date to start hardening off plants (leaving them
	 * outdoors during the day, bringing them inside at night)
	 */
	new_plant->hardening_off_date =
		new_plant->outdoor_planting_date;
	ret = add_days_to_date(&new_plant->hardening_off_date, -3);
	if (ret)
		return ret;

	/* Set sprouting base date */
	new_plant->sprouting_date = new_plant->seeding_date;
	return 0;
}

int calculate_direct_sown_plant_dates(struct plant *new_plant)
{
	int ret;
	int temp;

	new_plant->seeding_date = new_plant->outdoor_planting_date;
	
	new_plant->sprouting_date =
		new_plant->outdoor_planting_date;
	
	if (new_plant->num_weeks_until_outdoor_separation) {
		new_plant->outdoor_separation_date =
			new_plant->outdoor_planting_date;
		temp = new_plant->num_weeks_until_outdoor_separation;
		ret = add_days_to_date(
				&new_plant->outdoor_separation_date,
				temp*7);
		if (ret)
			return ret;
	}
	return 0;
}

int calculate_plant_dates(struct plant *new_plant)
{
	int ret;

	/* Some plants need to be direct sown outdoors,
	 * rather than started under a sun lamp indoors.
	 */
	if (new_plant->num_weeks_indoors) {
		ret = calculate_indoor_plant_dates(new_plant);
	} else {
		ret = calculate_direct_sown_plant_dates(new_plant);
	}
	if (ret)
		return ret;

	ret = add_days_to_date(&new_plant->sprouting_date,
			new_plant->avg_days_to_sprout);
	if (ret)
		return ret;

	/* Harvest date is calculated from the time the seed is in the soil,
	 * either indoors or outdoors.
	 */
	new_plant->harvest_date = new_plant->seeding_date;
	ret = add_days_to_date(&new_plant->harvest_date,
			new_plant->days_to_harvest);
	if (ret)
		return ret;

	return 0;
}


/****************** By plant calendar functions ******************/

/*
 * num seeds survived = num seeds planted * germination rate
 * num seeds survived / germination rate = num seeds planted
 */
float get_num_seeds_needed(struct plant *new_plant)
{
	return ceil(new_plant->num_plants_to_harvest /
			new_plant->germination_rate);
}

void print_date(char *description, struct tm *date)
{
	char string[MAX_NAME_LENGTH];

	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			date);
	printf("%s: %s\n", description, string);
}

void print_indoor_plant_dates(struct plant *new_plant)
{
	char string[MAX_NAME_LENGTH];
	float num_seeds;

	num_seeds = get_num_seeds_needed(new_plant);
	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->seeding_date);
	printf("Start %i seed%s under grow lamp: %s\n",
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "",
			string);
	print_date("Expect sprouting seeds around",
			&new_plant->sprouting_date);

	if (new_plant->num_weeks_until_indoor_separation)
		print_date("Separate or move to a bigger indoor pot",
				&new_plant->indoor_separation_date);

	print_date("Start hardening off seedlings",
			&new_plant->hardening_off_date);

	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->outdoor_planting_date);
	num_seeds = new_plant->num_plants_to_harvest;
	printf("Transplant %i plant%s outdoors: %s\n",
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "",
			string);
}

void print_direct_sown_plant_dates(struct plant *new_plant)
{
	char string[MAX_NAME_LENGTH];
	float num_seeds;

	num_seeds = get_num_seeds_needed(new_plant);
	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->outdoor_planting_date);
	printf("Direct sow %i seeds outdoors: %s\n",
			(int) num_seeds, string);
	print_date("Expect sprouting seeds around",
			&new_plant->sprouting_date);
	
	if (new_plant->num_weeks_until_outdoor_separation) {
		strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
				&new_plant->outdoor_separation_date);
		num_seeds = new_plant->num_plants_to_harvest;
		printf("Thin to %i plant%s: %s\n",
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "",
			string);
	}
}

void print_action_dates(struct plant *new_plant)
{
	int chars_printed;
	char string[MAX_NAME_LENGTH];

	chars_printed = printf("Calendar for %s:\n", new_plant->name);
	/* Don't count the newline */
	for(; chars_printed > 1; chars_printed--)
		putchar('=');
	printf("\n");

	if (new_plant->num_weeks_indoors)
		print_indoor_plant_dates(new_plant);
	else
		print_direct_sown_plant_dates(new_plant);

	strftime(string, MAX_NAME_LENGTH, "%a, %b. %d, %Y",
			&new_plant->harvest_date);
	if (new_plant->harvest_removes_plant)
		printf("Harvest plants: %s\n", string);
	else
		printf("Start harvesting: %s\n", string);

}

/****************** By month calendar functions ******************/

/* Is this time less than that time? */
int date_is_less_than(struct tm *this,
		struct tm *that)
{
	time_t this_time;
	time_t that_time;

	/* Turn both into epoch time */
	this_time = mktime(this);
	that_time = mktime(that);

	return this_time < that_time;
}

int dates_are_equal(struct tm *this, struct tm *that)
{
	time_t this_time;
	time_t that_time;

	/* Turn both into epoch time */
	this_time = mktime(this);
	that_time = mktime(that);

	return this_time == that_time;
}

int sort_in_one_date(struct plant_date *cal_entry,
		struct date_list **head_ptr)
{
	struct date_list **next_ptr = NULL;
	struct date_list *new_date;


	new_date = malloc(sizeof(*new_date));
	if (!new_date)
		return 0;
	new_date->cal_entry = cal_entry;

	/* This is where C++ operator overloading would be great. */
	for (next_ptr = head_ptr; *next_ptr != NULL;
			next_ptr = &(*next_ptr)->next) {
		/* If the new date is less than the next item in the list,
		 * insert it before that item in the list.
		 */
		if (date_is_less_than(cal_entry->date,
					(*next_ptr)->cal_entry->date)) {
			new_date->next = *next_ptr;
			*next_ptr = new_date;
			return 1;
		}
	}
	new_date->next = NULL;
	*next_ptr = new_date;
	return 1;
}

struct plant_date *make_calendar_entry(struct tm *date, char *blurb)
{
	struct plant_date *cal_entry;

	cal_entry = malloc(sizeof(*cal_entry));
	if (!cal_entry)
		return NULL;

	cal_entry->date = date;
	cal_entry->blurb = blurb;
	return cal_entry;
}

int insert_calendar_entry(struct tm *date, char *string,
		struct date_list **head_ptr)
{
	struct plant_date *cal_entry;

	cal_entry = make_calendar_entry(date, string);
	if (!cal_entry)
		return 0;

	if (!sort_in_one_date(cal_entry, head_ptr))
		return 0;
	return 1;
}

/* Organize the dates in the plant into a larger sorted date list */
int add_indoor_plant_dates_to_list(struct plant *new_plant,
		struct date_list **head_ptr)
{
	char *string;
	float num_seeds;

	if (!new_plant->num_weeks_indoors)
		return 0;

	num_seeds = get_num_seeds_needed(new_plant);
	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Start %i seed%s under grow lamp",
			new_plant->name,
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "");
	if (!insert_calendar_entry(&new_plant->seeding_date,
				string, head_ptr))
		return 0;

	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Expect sprouting seeds around",
			new_plant->name);
	if (!insert_calendar_entry(&new_plant->sprouting_date,
			       	string, head_ptr))
		return 0;

	if (new_plant->num_weeks_until_indoor_separation) {
		string = malloc(sizeof(char)*MAX_NAME_LENGTH);
		snprintf(string, MAX_NAME_LENGTH,
				"%s -- Separate or move to a bigger indoor pot",
				new_plant->name);
		if (!insert_calendar_entry(&new_plant->indoor_separation_date,
					string, head_ptr))
			return 0;
	}

	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Start hardening off seedlings",
			new_plant->name);
	if (!insert_calendar_entry(&new_plant->hardening_off_date,
			       	string, head_ptr))
		return 0;

	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	num_seeds = new_plant->num_plants_to_harvest;
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Transplant %i plant%s outdoors",
			new_plant->name,
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "");
	if (!insert_calendar_entry(&new_plant->outdoor_planting_date,
			       	string, head_ptr))
		return 0;
	return 1;
}

int add_direct_sown_plant_dates_to_list(struct plant *new_plant,
		struct date_list **head_ptr)
{
	char *string;
	float num_seeds;

	if (new_plant->num_weeks_indoors)
		return 0;

	num_seeds = get_num_seeds_needed(new_plant);
	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Direct sow %i seed%s outdoors",
			new_plant->name,
			(int) num_seeds,
			(num_seeds > 1) ? "s" : "");
	if (!insert_calendar_entry(&new_plant->outdoor_planting_date,
				string, head_ptr))
		return 0;

	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	snprintf(string, MAX_NAME_LENGTH,
			"%s -- Expect sprouting seeds around",
			new_plant->name);
	if (!insert_calendar_entry(&new_plant->sprouting_date,
			       	string, head_ptr))
		return 0;

	if (new_plant->num_weeks_until_outdoor_separation) {
		string = malloc(sizeof(char)*MAX_NAME_LENGTH);
		num_seeds = new_plant->num_plants_to_harvest;
		snprintf(string, MAX_NAME_LENGTH,
				"%s -- Thin to %i plant%s",
				new_plant->name,
				(int) num_seeds,
				(num_seeds > 1) ? "s" : "");
		if (!insert_calendar_entry(&new_plant->outdoor_separation_date,
					string, head_ptr))
			return 0;
	}
	return 1;
}

int add_harvest_dates_to_list(struct plant *new_plant,
		struct date_list **head_ptr)
{
	char *string;
	unsigned int num_plants;

	string = malloc(sizeof(char)*MAX_NAME_LENGTH);
	num_plants = new_plant->num_plants_to_harvest;
	if (new_plant->harvest_removes_plant)
		snprintf(string, MAX_NAME_LENGTH,
				"%s -- Harvest %u plant%s",
				new_plant->name,
				num_plants,
				(num_plants > 1) ? "s": "");
	else
		snprintf(string, MAX_NAME_LENGTH,
				"%s -- Start harvesting",
				new_plant->name);
	if (!insert_calendar_entry(&new_plant->harvest_date,
			       	string, head_ptr))
		return 0;

	return 1;
}

void print_month_and_year(struct tm *new_date)
{
	char string[MAX_NAME_LENGTH];
	int chars_printed;

	strftime(string, MAX_NAME_LENGTH,
			"\n%B %Y\n", new_date);
	chars_printed = printf(string);
	/* Don't count the newline */
	for(; chars_printed > 1; chars_printed--)
		putchar('=');
	printf("\n");
}

void print_by_month_calendar(struct date_list *head)
{
	int cur_month, cur_year;
	struct tm *old_date = NULL;
	struct tm *new_date;
	struct date_list *item;
	char string[MAX_NAME_LENGTH];

	if (!head)
		return;

	new_date = head->cal_entry->date;
	print_month_and_year(new_date);
	cur_month = new_date->tm_mon;
	cur_year = new_date->tm_year;

	for (item = head; item != NULL; item = item->next) {
		new_date = item->cal_entry->date;
		if (cur_month != new_date->tm_mon ||
				cur_year != new_date->tm_year) {
			printf("\n");
			print_month_and_year(new_date);
			cur_month = new_date->tm_mon;
			cur_year = new_date->tm_year;
		}
		strftime(string, MAX_NAME_LENGTH, "%e (%a)",
				new_date);
		if (old_date == NULL ||
				!dates_are_equal(old_date, new_date))
			printf("\n   %s: %s\n", string,
					item->cal_entry->blurb);
		else
			printf("             %s\n",
					item->cal_entry->blurb);
		old_date = new_date;
	}
}

#define	BY_PLANT	(1 << 0)
#define	BY_MONTH	(1 << 1)
#define	BY_HARVEST	(1 << 2)

int main (int argc, char *argv[])
{
	FILE *fp;
	struct plant *new_plant;
	struct date_list *head = NULL;
	struct date_list *harvest_head = NULL;
	unsigned int chars_printed;
	unsigned int calendar_bitmask = 0;
	int i;

	if (argc < 2) {
		printf("Help: plant <file> [output type]...\n");
		printf("Where [output type] can be:\n");
		printf("  p for a by-plant calendar\n");
		printf("  m for a by-month calendar\n");
		printf("  h for a harvest calendar\n");
		return -1;
	}
	fp = fopen(argv[1], "r");
	if (!fp) {
		printf("Bad file.\n");
		return -1;
	}

	for (i = 2; i < (2+3) && i < argc; i++) {
		if (!strcmp(argv[i], "p"))
			calendar_bitmask |= BY_PLANT;
		if (!strcmp(argv[i], "m"))
			calendar_bitmask |= BY_MONTH;
		if (!strcmp(argv[i], "h"))
			calendar_bitmask |= BY_HARVEST;
	}

	while (1) {
		new_plant = parse_and_create_plant(fp);
		if (!new_plant)
			break;
		calculate_plant_dates(new_plant);
		if (calendar_bitmask & BY_PLANT) {
			printf("\n");
			print_action_dates(new_plant);
			printf("\n");
		}
		add_indoor_plant_dates_to_list(new_plant,
				&head);
		add_direct_sown_plant_dates_to_list(new_plant,
				&head);
		add_harvest_dates_to_list(new_plant,
				&harvest_head);
	}

	if (calendar_bitmask & BY_MONTH) {
		chars_printed = printf("\n\nSeedling By-Month Calendar\n");
		for(; chars_printed > 3; chars_printed--)
			putchar('*');
		printf("\n");
		print_by_month_calendar(head);
	}

	if (calendar_bitmask & BY_HARVEST) {
		chars_printed = printf("\n\nHarvest Calendar\n");
		for(; chars_printed > 3; chars_printed--)
			putchar('*');
		printf("\n");
		print_by_month_calendar(harvest_head);
	}

	return 0;
}
