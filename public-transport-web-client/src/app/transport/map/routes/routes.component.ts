import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Stop} from "../../../http/stop.service";

@Component({
  selector: 'app-routes',
  templateUrl: './routes.component.html',
  styleUrl: './routes.component.scss'
})
export class RoutesComponent {

  @Input() stop: Stop | null;

  @Output() clickLine = new EventEmitter<string>();

  constructor() {
  }

  onClickLine(line: string) {
    this.clickLine.emit(line);
  }

  public getTrams(): string[] {
    return (this.stop?.lines || [])
      .filter(line => this.isTram(line))
      .sort((prev, curr) => Number(prev) - Number(curr));
  }

  public hasTrams(): boolean {
    return this.getTrams().length > 0;
  }

  public getBuses(): string[] {
    return (this.stop?.lines || [])
      .filter(line => !line.startsWith('N'))
      .filter(line => !this.isTram(line))
      .sort();
  }

  public hasBuses(): boolean {
    return this.getBuses().length > 0;
  }

  public getNightBus(): string[] {
    return (this.stop?.lines || [])
      .filter(line => line.startsWith('N')).sort();
  }

  public hasNightBuses(): boolean {
    return this.getNightBus().length > 0;
  }

  private isTram(value: string): boolean {
    const tramNumber = Number(value);
    return !isNaN(tramNumber) && tramNumber < 100;
  }

  public isFast(line: string) {
    return line.startsWith('5') && line.length === 3;
  }

}
