import {NgModule} from '@angular/core';
import {OtpComponent} from "./otp.component";
import {MatInputModule} from "@angular/material/input";
import {FormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {OtpService} from "../../../http/otp.service";
import {ItineraryComponent} from "./itinerary/itinerary.component";
import {ItineraryWalkComponent} from "./itinerary-walk/itinerary-walk.component";
import {ItineraryTransitComponent} from "./itinerary-transit/itinerary-transit.component";
import {ItineraryDetailsComponent} from "./itinerary-details/itinerary-details.component";
import {SharedModule} from "../../../shared/shared.module";
import {CommonModule} from "@angular/common";
import {MatExpansionModule} from "@angular/material/expansion";
import {ItineraryHeaderComponent} from "./itinerary-header/itinerary-header.component";
import {MatGridListModule} from "@angular/material/grid-list";
import {MatCardModule} from "@angular/material/card";
import {ItineraryBikeHeaderComponent} from "./itinerary-bike-header/itinerary-bike-header.component";
import {ItineraryBikeDetailsComponent} from "./itinerary-bike-details/itinerary-bike-details.component";
import {MatRippleModule} from "@angular/material/core";
import {SummaryJourneyComponent} from "./summary-journey/summary-journey.component";
import {MatDialogModule} from "@angular/material/dialog";
import {JourneyDistancePipe} from "./summary-journey/journey-distance.pipe";
import {JourneyDurationPipe} from "./summary-journey/journey-duration.pipe";
import {MatDividerModule} from "@angular/material/divider";
import {MatAutocompleteModule} from "@angular/material/autocomplete";


@NgModule({
    declarations: [
        OtpComponent,
        ItineraryComponent,
        ItineraryBikeHeaderComponent,
        ItineraryBikeDetailsComponent,
        ItineraryHeaderComponent,
        ItineraryDetailsComponent,
        ItineraryWalkComponent,
        ItineraryTransitComponent,
        SummaryJourneyComponent,
        JourneyDistancePipe,
        JourneyDurationPipe
    ],
    exports: [
        OtpComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatExpansionModule,
        MatGridListModule,
        MatCardModule,
        MatRippleModule,
        MatDialogModule,
        MatDividerModule,
        MatInputModule,
        MatAutocompleteModule
    ],
    providers: [
        OtpService
    ]
})
export class OtpModule {
}
