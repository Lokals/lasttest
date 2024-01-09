Nasz system gromadzi informacje o osobach,
Mamy pracowników, studentów, emerytów, w przyszłości może być więcej osób.
Każda osoba ma imię, nazwisko, pesel [nie moze sie powtarzac], wzrost, wage, adres email.
dla studentow pamietamy nazwe ukonczonej uczelnii, rok studiow, kierunek studiow, wysokosc otrzymywanego stypendium.
dla pracownika pamietamy date rozpoczecia zatrudnienia, aktualne stanowisko, aktualną pensję.
dla emeryta pamiętamy wysokość emerytury, ilosc przepracowanych lat.

Chcemy miec JEDEN [dokladnie jeden endpoint]
za pomoca ktorego moge pobierac osoby z bazy danych po roznych kryteriach wyszukiwaniach,
np: po typie, po imieniu, po nazwisku, po wieku, po peselu, po plci, po wzroscie od do, po wadze od do, po adresie emamil.
dla pracownikow po aktualnej pensji od, do
dla studentow po nazwie aktualnej uczelni.
itp itd.
forma wyszukiwania jest prosta, dla rzeczy liczbowych / dat: zawsze podajemy przedzial <od, do> [inclusive]
dla napisow, stosujemy wyszukiwanie metoda: "contains ignore case"

dla endpointu chcemy miec tez mozliwosc paginacji.

Chcemy miec JEDEN [dokladnie jeden endpoint]
do dodawania dowolnego typu osoby do naszego systemu, kontrakt musisz opracowac sam, ale kluczowe wymaganie ma byc takie ze:
"dodanie nowego typu osoby ma byc mozliwe w taki sposob ze nie musze modyfikowac zadnej z klas - tylko dodac nowe"
podczas dodawania ma byc zrobiona sensowna walidacja oraz obsluga bledow.

Chcemy miec JEDEN [dokladnie jeden endpoint]
do edycji dowolnej osobyw naszym programie,
uwaga: podczas edycji musisz obsluzyc nastepujacy problem: dane nie mogą być ot tak nadpisane przez inna transakcje,
jeśli edytujesz dane, a coś właśnie zmieniło dane ktore edytujesz, to powinien wyskoczyc odpowiedni wyjatek.

Chcemy miec endpoint do zarządzania stanowiskami danego pracownika.
pracownik na danym stanowisku moze pracowac <od, do>, na stanowisku o nazwie XYZ i otrzymujac pensje ABC.
stanowiska nie mogą się pokrywać datami (daty nie moga na siebie nachodzic)
nalezy tez zabezpieczyc przypisywanie stanowiska w taki sposob aby daty nie mogly sie pokrywac z istniejacymi stanowiskami.

Chcemy miec endpoint ktory pozwoli nam załadować plik csv z osobami do naszej bazy danych.
założenia:
- chcemy aby mozna było wrzucic dowolnie duzy plik, np: 3GB
- chcemy aby import wykonywał się w sposób nieblokujący i abyśmy mogli w formie datkowego endpointu zerknac co sie z nim dzieje [status, data stworzenia, data rozpoczecia, ilosc przeprocesowanych wierszy]
- plik csv wyglada tak:
  TYP,imie,nazwisko,pesel,wzrost,waga,adres email,pozostale parametry dla konkretnej osoby
- chcemy sprawic aby tylko jeden import w danym czasie mogl sie wykonywac.

Security:
endpointy sa zabezpieczone security [uzytkownikow mozesz miec w bazie danych/ w pamieci/ na razie to wszystko jedno]
Dodawac osoby moze tylko ADMIN
Edytowac osoby moze admin
Importowac osoby moze tylko admin lub importer.
przypisywac stanowisko moze tylko ADMIN lub inny EMPLOYEE


Testy:
calosc nalezy pokryc testami integracyjnymi.
Uzyj spring boot 3.0.x