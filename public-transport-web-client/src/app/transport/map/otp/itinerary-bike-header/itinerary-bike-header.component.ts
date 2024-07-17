import {Component} from '@angular/core';
import {ItineraryComponent} from "../itinerary/itinerary.component";
import moment from "moment";

@Component({
    selector: 'app-itinerary-bike-header',
    templateUrl: './itinerary-bike-header.component.html',
    styleUrls: [
        '../itinerary/itinerary.component.scss',
        './itinerary-bike-header.component.scss'
    ]
})
export class ItineraryBikeHeaderComponent extends ItineraryComponent {

    public now(): moment.Moment {
        return moment();
    }

    public getKcal(): number {
        const kcal: number[] = (this.itinerary.legs || [])
            .map(leg => {
                switch (leg.mode) {
                    case 'WALK':
                        return (leg?.duration || 0) / 3600 * 250;
                    case 'BICYCLE':
                        return (leg?.duration || 0) / 3600 * 500;
                    default:
                        return 0;
                }
            });

        return kcal.reduce((sum, value) => sum + value, 0);
    }

}
