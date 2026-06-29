package se.fk.github.bekraftabeslutbff;

import se.fk.github.bekraftabeslutbff.model.*;

import java.util.List;

public class BekraftabeslutMapper
{

   public static BekraftabeslutResponse transform(RawBekraftabeslutResponse raw)
   {
      BekraftabeslutResponse result = new BekraftabeslutResponse();
      result.handlaggningId = raw.handlaggningId;

      Kund kund = new Kund();
      kund.fornamn = raw.kund.fornamn;
      kund.efternamn = raw.kund.efternamn;
      kund.kon = raw.kund.kon;
      if (raw.kund.anstallning != null)
      {
         Anstallning anstallning = new Anstallning();
         anstallning.anstallningsdag = raw.kund.anstallning.anstallningsdag;
         anstallning.arbetstidProcent = raw.kund.anstallning.arbetstidProcent;
         anstallning.sistaAnstallningsdag = raw.kund.anstallning.sistaAnstallningsdag;
         anstallning.organisationsnamn = raw.kund.anstallning.organisationsnamn;
         anstallning.organisationsnummer = raw.kund.anstallning.organisationsnummer;
         kund.anstallning = anstallning;
      }
      result.kund = kund;

      result.ersattning = raw.ersattning.stream().map(e -> {
         Ersattning ersattning = new Ersattning();
         ersattning.ersattningId = e.ersattningId;
         ersattning.ersattningstyp = e.ersattningstyp;
         ersattning.omfattningProcent = e.omfattningProcent;
         ersattning.belopp = e.belopp;
         ersattning.berakningsgrund = e.berakningsgrund;
         ersattning.beslutsutfall = e.beslutsutfall;
         ersattning.avslagsanledning = e.avslagsanledning;
         ersattning.from = e.from;
         ersattning.tom = e.tom;
         return ersattning;
      }).toList();

      return result;
   }
}
