CREATE TABLE public.note (
   flag BOOLEAN DEFAULT FALSE NOT NULL,
   event_id UUID NOT NULL,
   id UUID NOT NULL,
   content TEXT NOT NULL,
   CONSTRAINT note_pkey PRIMARY KEY (id)
);

CREATE TABLE public.events_template (
   end_time time(6) WITHOUT TIME ZONE NOT NULL,
   start_time time(6) WITHOUT TIME ZONE NOT NULL,
   id UUID NOT NULL,
   user_id UUID NOT NULL,
   week_day TEXT,
   description TEXT NOT NULL,
   CONSTRAINT events_template_pkey PRIMARY KEY (id)
);

CREATE TABLE public.events_current (
  id UUID NOT NULL,
   description TEXT NOT NULL,
   start_time time WITHOUT TIME ZONE NOT NULL,
   end_time time WITHOUT TIME ZONE NOT NULL,
   "date" date NOT NULL,
   template_id UUID,
   user_id UUID,
   CONSTRAINT pk_events_current PRIMARY KEY (id)
);

CREATE TABLE public.profile (
   id UUID NOT NULL,
   name TEXT NOT NULL,
   CONSTRAINT profile_pkey PRIMARY KEY (id)
);

ALTER TABLE ONLY public.note
    ADD  CONSTRAINT fk6010ipgibhm731r4n9loxrb4v FOREIGN KEY (event_id) REFERENCES public.events_current(id);

ALTER TABLE ONLY public.events_template
    ADD CONSTRAINT fke6wpdvdfpymq9cdq7wrnswotu FOREIGN KEY (user_id) REFERENCES public.profile(id);

ALTER TABLE ONLY public.events_current
    ADD CONSTRAINT fke7xlvwojiylwnt4shf4kpjjog FOREIGN KEY (template_id) REFERENCES public.events_template(id);

ALTER TABLE ONLY public.events_current
    ADD CONSTRAINT fkmiptb3u8r8ymj332b1k3xhdy5 FOREIGN KEY (user_id) REFERENCES public.profile(id);
