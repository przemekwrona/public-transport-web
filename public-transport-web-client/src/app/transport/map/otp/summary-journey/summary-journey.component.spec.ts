import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SummaryJourneyComponent } from './summary-journey.component';

describe('SummaryJourneyComponent', () => {
  let component: SummaryJourneyComponent;
  let fixture: ComponentFixture<SummaryJourneyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryJourneyComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SummaryJourneyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
