export function getMockBeslutsdata(handlaggningId: string) {
  return {
    handlaggning_id: handlaggningId,
    kund: {
      fornamn: "Lisa",
      efternamn: "Tass",
      kon: "KVINNA",
      anstallning: {
        organisationsnamn: "Mock AB",
        arbetstid_procent: 100,
        lon: {
          lonesumma: 40000,
        },
      },
    },
    ersattning: [
      {
        ersattning_id: `item-${handlaggningId}-1`,
        ersattningstyp: "HUNDBIDRAG",
        omfattning_procent: 100,
        belopp: 40000,
        berakningsgrund: 40000,
        beslutsutfall: "FU",
        from: "2025-01-10",
        tom: "2025-01-10",
      },
    ],
  };
}
