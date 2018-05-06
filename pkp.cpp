#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>

using namespace std;

const int MLP = 50;
const int LDT = 7; //liczba dni tygodnia
const int LG = 12; //liczba godzin
const int GP = 8; //

int liczba_pasazerow_w_tyg(int Bus[LDT][LG]) {
	int suma = 0; //short, int, double, float
	for (int dzien = 0; dzien < LDT; dzien++)
		for (int godzina = 0; godzina < LG; godzina++)
			suma += Bus[dzien][godzina];
	return suma;
}

int liczba_pasazerow_w_ciagu_dnia(int Bus[LDT][LG], int dzien) {
	int suma = 0;
	for (int godzina = 0; godzina < LG; godzina++)	suma += Bus[dzien][godzina];
	return suma;
}

void policz_srednia(int Bus[LDT][LG]) {
	double srednia[LG];
	for (int godzina = 0; godzina < LG; godzina++) {
		srednia[godzina] = 0;
		for (int dzien = 0; dzien < LDT; dzien++)
			srednia[godzina] += Bus[dzien][godzina];
		srednia[godzina] = srednia[godzina] / (1.0*LDT);
		cout << "Srednia liczba pasazerow o godzinie: " << godzina + 8 << ":00 wynosila " << srednia[godzina] << endl;
	}
}

int max_pasazerow_w_dniu(int Bus[LDT][LG], int dzien) {
		int maximum = 0;
		for (int godzina = 1; godzina <LG; godzina++) {
			if (Bus[dzien][godzina] > maximum) maximum = Bus[dzien][godzina];
		}		
		return maximum; //zwracamy maxymalna liczbe pasazerow z danego dnia
}

void pisz_max_w_dniu(int Bus[LDT][LG], int dzien) {
	cout << dzien + 1 << " dnia najwieksza lioczba pasazerow to " << max_pasazerow_w_dniu(Bus, dzien) << endl;
}

void wypisz_ile_pasazerow_w_dniach(int Bus[LDT][LG]) {
	for (int dzien = 0; dzien < LDT; dzien++) {
		cout << " dnia " << dzien+1 << " jechalo " << liczba_pasazerow_w_ciagu_dnia(Bus, dzien) << " pasazerow" << endl;
	}
	cout << endl;
}



void max_tygodniowy_pasazerow(int Bus[LDT][LG]) {
	int pomocnicza_zmienna = 0;
	int maximum = 0;
	for (int dzien = 0; dzien < LDT; dzien++) {		
		pomocnicza_zmienna = max_pasazerow_w_dniu(Bus, dzien);
		if (pomocnicza_zmienna > maximum) maximum = pomocnicza_zmienna;
	}
	cout << "Najwieksza lioczba pasazerow w tygodniu to " << maximum << endl;
}

int min_pasazerow_w_dniu(int Bus[LDT][LG], int dzien) {
	int min_godzina = 0;
	int minimum_pasazerow = Bus[dzien][0];
	for (int godzina = 0; godzina < LG; godzina++)
		if (Bus[dzien][godzina] < minimum_pasazerow) {
			minimum_pasazerow = Bus[dzien][min_godzina];
			min_godzina = godzina;
		}
	//cout << dzien + 1 << " dnia najmniej pasazerow jechalo o godzinie " << min_godzina + 8 << ":00" << endl;
	return min_godzina; //zwracamy indeks pod ktorym w macierzy byla najmniejsza liczba pasazerow
}

void pisz_min_w_dniu(int Bus[LDT][LG], int dzien) {
	cout << dzien + 1 << " dnia najmniejsza lioczba pasazerow to " << Bus[dzien][min_pasazerow_w_dniu(Bus, dzien)] << endl;
}

int main() {
	int Bus[LDT][LG];
	srand(time(NULL));
	for (int dzien = 0; dzien < LDT; dzien++) {
		for (int godzina = 0; godzina < LG; godzina++) {
			Bus[dzien][godzina] = rand() % MLP + 1;
		}
	}

	cout << setw(4) << " |";
	for (int godzina = GP; godzina < GP + LG; godzina++) {
		cout << setw(4) << godzina;
	}
	cout << endl; //now linia == enter w wordzie

	cout << "----";
	for (int godzina = GP; godzina < GP + LG; godzina++) {
		cout << "----";
	}
	cout << endl;

	for (int dzien = 0; dzien < LDT; dzien++) {
		cout << "D" << dzien + 1 << " |";
		for (int godzina = 0; godzina<LG; godzina++)
			cout << setw(4) << Bus[dzien][godzina]; \
			cout << endl;
	}

	//cout << "----";
	for (int godzina = GP; godzina < GP + LG + 1; godzina++)	cout << "----";

	cout << endl;

	cout << "W tygodniu jechalo " << liczba_pasazerow_w_tyg(Bus) << " pasazerow" << endl;

	
	cout << endl;


	//okreslamy ktorego dnia jechalo najwiecej pasazerow
	

	cout << endl;

	//okreslamy o ktorej godzinie danego dnia jechalo najmniej pasazerow
	for (int dzien = 0; dzien < LDT; dzien++)
	{
		pisz_min_w_dniu(Bus, dzien);
		pisz_max_w_dniu(Bus, dzien);
	}


	return 0;
}
