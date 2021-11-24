## Use whatever base image
FROM obsidiandynamics/kafdrop

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

ENTRYPOINT /wait && /kafdrop.sh