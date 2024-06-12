import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Stop} from "../../../http/stop.service";
import {TimetableDeparture} from "../../../http/timetable.service";
import {groupBy} from "lodash";
import moment from 'moment';
import {DepartureMetadata} from "../timetable-content/timetable-content.component";


@Component({
  selector: 'app-timetable',
  templateUrl: './timetable.component.html',
  styleUrl: './timetable.component.scss'
})
export class TimetableComponent {

  private _timetables: { [line: string]: { [date: string]: TimetableDeparture[]; } };

  @Input() set timetables(value: { [line: string]: { [date: string]: TimetableDeparture[]; } }) {
    this._timetables = value;
  }

  get timetables(): { [line: string]: { [date: string]: TimetableDeparture[]; } } {
    return this._timetables;
  }

  private _line: string = '';

  @Input() set line(line: string) {
    this._line = line;
  }

  get line(): string {
    return this._line;
  }

  @Input() stop: Stop | null;
  @Input() direction: string;
  @Output() onClickDeparture = new EventEmitter<DepartureMetadata>();

  public timetableDate: moment.Moment = moment();

  public getTimetable(date: moment.Moment): TimetableDeparture[] {
    return this.timetables[this.line][date.format('yyyy-MM-DD')] || [];
  }

  public findTimetableByLineAndDate(line: string, date: moment.Moment): TimetableDeparture[] {
    if (this.timetables == null) {
      return [];
    }
    if (this.timetables[line] == null) {
      return [];
    }
    return this.timetables[line][date.format('yyyy-MM-DD')] || [];
  }

  public getMainHeadsing(): string {
    const headsigns: string[] = this.findTimetableByLineAndDate(this.line, this.timetableDate).map(timetable => timetable.headsign);
    return headsigns[0];
  }

  clickDeparture(timetableDeparture: DepartureMetadata) {
    this.onClickDeparture.emit(timetableDeparture);
  }

  getTimetableDates(): moment.Moment[] {
    return [
      moment(),
      moment().add(1, 'days'),
      moment().add(2, 'days'),
      moment().add(3, 'days'),
      moment().add(4, 'days'),
      moment().add(5, 'days'),
      moment().add(6, 'days')
    ];
  }

}
