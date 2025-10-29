package pl.wrona.webserver.bussiness.pdf.trip;

import pl.wrona.webserver.core.agency.TripVariantMode;

public interface TripMode {

    String getLine();
    String getName();
    TripVariantMode getMode();
}
