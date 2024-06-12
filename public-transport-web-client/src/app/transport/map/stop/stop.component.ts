import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {Stop} from "../../../http/stop.service";
import {interval, Subscription} from "rxjs";
import moment from "moment";

@Component({
  selector: 'app-stop',
  templateUrl: './stop.component.html',
  styleUrl: './stop.component.scss'
})
export class StopComponent implements OnInit, OnDestroy {

  private intervalTimer = interval(1000);
  private intervalSubscription: Subscription;

  @Input() stop: Stop | null;

  public currentDate: moment.Moment = moment();

  ngOnInit(): void {
    this.intervalSubscription = this.intervalTimer.subscribe(number => {
      this.currentDate = moment();
    });
  }

  ngOnDestroy(): void {
    this.intervalSubscription.unsubscribe();
  }

  public hasTrams(): boolean {
    return (this.stop?.types || []).includes(0);
  }

  public hasBuses(): boolean {
    return (this.stop?.types || []).includes(3);
  }

}
