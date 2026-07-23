import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrigadeSchedulerComponent } from './brigade-scheduler.component';

describe('BrigadeSchedulerComponent', () => {
  let component: BrigadeSchedulerComponent;
  let fixture: ComponentFixture<BrigadeSchedulerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeSchedulerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BrigadeSchedulerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
