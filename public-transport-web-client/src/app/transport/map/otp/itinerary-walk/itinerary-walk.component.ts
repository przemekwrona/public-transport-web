import {Component, OnInit} from '@angular/core';
import moment from 'moment';
import {ItineraryComponent} from "../itinerary/itinerary.component";

@Component({
  selector: 'app-itinerary-walk',
  templateUrl: './itinerary-walk.component.html',
  styleUrls: [
    '../itinerary/itinerary.component.scss',
    './itinerary-walk.component.scss'
  ]
})
export class ItineraryWalkComponent extends ItineraryComponent {

  public now(): moment.Moment {
    return moment();
  }

  public getKcal(): number {
    const leg = this.getFirst();
    const difference = moment(leg.endTime).diff(leg.startTime, 'minutes')
    return (261 * difference) / 60;
  }

}
