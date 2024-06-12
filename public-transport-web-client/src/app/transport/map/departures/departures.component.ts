import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {interval, Subscription} from "rxjs";
import moment from 'moment';
import {Stop} from "../../../http/stop.service";
import {Departure} from "../../../http/timetable.service";

@Component({
  selector: 'app-departures',
  templateUrl: './departures.component.html',
  styleUrl: './departures.component.scss'
})
export class DeparturesComponent implements OnInit, OnDestroy {

  private intervalTimer = interval(1000);
  private intervalSubscription: Subscription;

  private _line: string | null;

  @Input() set line(line: string | null) {
    this._line = line;
    if (this.line != null) {
      this.numberOfDepartures = 3;
    }
    this.numberOfDepartures = this.countNumberOfLines();
  }

  get line(): string | null {
    return this._line;
  }

  @Input() stop: Stop | null;

  private _departures: Departure[];

  @Input() set departures(departures: Departure[]) {
    this._departures = departures;
    this.numberOfDepartures = this.countNumberOfLines();
  }

  @Output() onClick = new EventEmitter<string>();

  get departures(): Departure[] {
    return this._departures;
  }

  public currentDate: moment.Moment = moment();
  public numberOfDepartures: number = 0;
  public isOpen: boolean = true;

  constructor() {
  }

  ngOnInit(): void {
    this.intervalSubscription = this.intervalTimer.subscribe(number => {
      this.currentDate = moment();
      const currentDeparture = this.departures[0];

      if (currentDeparture != null) {
        const departureDifference = moment(currentDeparture.date).diff(this.currentDate);

        if (departureDifference < 0) {
          this.departures.shift();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.intervalSubscription.unsubscribe();
  }

  countNumberOfLines(): number {
    if (this.hasLine()) {
      return 2;
    }

    const now = moment();

    const countDeparturesUnder1H = this.departures.slice(0, 10)
      .map(departure => moment(departure.date))
      .map(time => time.diff(now, 'minutes'))
      .filter(time => time <= 60).length;

    if (countDeparturesUnder1H <= 2) {
      return 2;
    } else if (countDeparturesUnder1H >= 6) {
      return 6;
    } else {
      if (countDeparturesUnder1H % 2 == 0) {
        return countDeparturesUnder1H;
      } else {
        return countDeparturesUnder1H + 1;
      }
    }
  }

  hasLine(): boolean {
    return this.line != null;
  }

  hasDepartures(): boolean {
    return this.departures != null && this.departures.length > 0;
  }

  lessThan1Min(date: Date): boolean {
    const duration =  moment.duration(moment(date).diff(moment()));
    return duration.asSeconds() < 120;
  }

  showShape(shapeId: string) {
    this.onClick.emit(shapeId);
  }

}
