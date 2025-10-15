import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {interval, Subscription} from "rxjs";
import moment from "moment";
import {Route, Stop, StopDetails} from "../../../generated/public-transport-planner-api";

@Component({
  selector: 'app-stop',
  templateUrl: './stop.component.html',
  styleUrl: './stop.component.scss'
})
export class StopComponent implements OnInit, OnDestroy {

  private intervalTimer = interval(1000);
  private intervalSubscription: Subscription;

  @Input() stop: Stop | null;
  @Input() stopDetails: StopDetails | null;
  @Input() routes: Route[] = [];

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
    // return (this.stop?.types || []).includes(0);
    return false;
  }

  public hasBuses(): boolean {
    // return (this.stop?.types || []).includes(3);
    return true;
  }

}
