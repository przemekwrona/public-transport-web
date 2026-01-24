import {Component, OnInit} from '@angular/core';
import moment from 'moment';
import {ItineraryComponent} from "../itinerary/itinerary.component";

@Component({
    selector: 'app-itinerary-walk',
    templateUrl: './itinerary-walk.component.html',
    styleUrls: [
        '../itinerary/itinerary.component.scss',
        './itinerary-walk.component.scss'
    ],
    standalone: false
})
export class ItineraryWalkComponent extends ItineraryComponent {

  public now(): moment.Moment {
    return moment();
  }

  public getKcal(): number {
    return  300 * (this.getFirst().duration || 0) / 3600;
  }

}
