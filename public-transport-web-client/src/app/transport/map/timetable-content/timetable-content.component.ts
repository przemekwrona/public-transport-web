import {Component, EventEmitter, Input, Output} from '@angular/core';
import moment from "moment";
import {TimetableDeparture} from "../../../http/timetable.service";

export class DepartureMetadata {
  tripId: string;
  shapeId: string;
}

@Component({
  selector: 'app-timetable-content',
  templateUrl: './timetable-content.component.html',
  styleUrl: './timetable-content.component.scss'
})
export class TimetableContentComponent {

  private _timetables: TimetableDeparture[];

  @Input() set timetables(timetables: TimetableDeparture[]) {
    this._timetables = timetables;
    this.groupedTimetables = this.getTimetable();
    this.closestDeparture = this.findFirstDeparture(this.groupedTimetables);
  }

  get timetables(): TimetableDeparture[] {
    return this._timetables;
  }

  @Input() timetableDate: moment.Moment;

  @Input() line: string = '';

  @Output() onClickDeparture = new EventEmitter<DepartureMetadata>();

  public groupedTimetables: { [hour: number]: TimetableDeparture[] };
  public closestDeparture: TimetableDeparture | null;

  public hasTimetables() {
    return this.timetables.length > 0;
  }

  public hasHour(hour: number): boolean {
    return this.groupedTimetables[hour] != null && this.groupedTimetables[hour].length > 0;
  }

  public isClose(date: Date): boolean {
    return this.closestDeparture != null
      && moment(date).isSame(this.closestDeparture.departure)
      && moment(this.closestDeparture.departure).diff(moment(), 'hours') < 4;
  }

  showTimetable(timetable: TimetableDeparture) {
    const metadata = new DepartureMetadata();
    metadata.tripId = timetable.tripId;
    metadata.shapeId = timetable.shapeId;
    this.onClickDeparture.emit(metadata);
  }

  private getTimetable(): { [hour: number]: TimetableDeparture[] } {
    return this.timetables
      .reduce((group: { [hour: number]: TimetableDeparture[] }, product: TimetableDeparture) => {
        const category = moment(product.departure).hour();
        group[category] = group[category] ?? [];
        group[category].push(product);
        return group;
      }, {});
  }

  private findFirstDeparture(timetable: { [hour: number]: TimetableDeparture[] }): TimetableDeparture | null {
    const now: moment.Moment = moment();

    // if (!now.isSame(this.timetableDate)) {
    //   return null;
    // }

    const currentHours: TimetableDeparture[] = (timetable[now.hour()] || []).filter(timetable => moment(timetable.departure).isAfter(now));

    if (currentHours.length > 0) {
      return currentHours[0];
    }

    const nextHours: TimetableDeparture[] = timetable[now.hour() + 1] || [];

    if (nextHours.length > 0) {
      return nextHours[0];
    }

    return null;
  }

  public hasClose(hour: number): boolean {
    return moment(this.closestDeparture?.departure).hour() === hour
      && moment(this.closestDeparture?.departure).diff(moment(), 'hours') < 4;
  }

  public getClosesDepartureInMinutes(): number {
    return moment(this.closestDeparture?.departure).diff(moment(), 'minutes') + 1;
  }

  public getHours(): number[] {
    if (this.line.startsWith('N')) {
      if (this.hasHour(22) && this.hasHour(23) && this.hasHour(5)) {
        return [22, 23, 0, 1, 2, 3, 4, 5];
      }

      if (this.hasHour(23) && this.hasHour(5)) {
        return [23, 0, 1, 2, 3, 4, 5];
      }

      if (this.hasHour(5)) {
        return [0, 1, 2, 3, 4, 5];
      }

      return [22, 23, 0, 1, 2, 3, 4, 5];
    }

    if (this.hasHour(4) && this.hasHour(5) && this.hasHour(6)) {
      if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
        return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
      }
      if (this.hasHour(22) && this.hasHour(23)) {
        return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
      }
      if (this.hasHour(22)) {
        return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
      }
      return [4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
    }

    if (this.hasHour(5) && this.hasHour(6)) {
      if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
        return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
      }
      if (this.hasHour(22) && this.hasHour(23)) {
        return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
      }
      if (this.hasHour(22)) {
        return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
      }
      return [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
    }

    if (this.hasHour(6)) {
      if (this.hasHour(22) && this.hasHour(23) && this.hasHour(0)) {
        return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];
      }
      if (this.hasHour(22) && this.hasHour(23)) {
        return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
      }
      if (this.hasHour(22)) {
        return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22];
      }
      return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21];
    }


    return [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 0];

  }


}
